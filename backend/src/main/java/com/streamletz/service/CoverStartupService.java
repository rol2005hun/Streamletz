package com.streamletz.service;

import com.streamletz.model.Track;
import com.streamletz.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoverStartupService {
    private final TrackRepository trackRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${music.covers.path:/covers}")
    private String coversPath;

    @Value("${music.storage.path}")
    private String musicStoragePath;

    private static final int TARGET_SIZE = 400;
    private static final Color[][] GRADIENT_COLORS = {
            { new Color(138, 43, 226), new Color(75, 0, 130) },
            { new Color(255, 20, 147), new Color(220, 20, 60) },
            { new Color(30, 144, 255), new Color(0, 191, 255) },
            { new Color(255, 69, 0), new Color(255, 140, 0) },
            { new Color(148, 0, 211), new Color(72, 61, 139) },
            { new Color(0, 128, 128), new Color(0, 191, 191) },
            { new Color(255, 0, 127), new Color(127, 0, 255) },
            { new Color(220, 20, 60), new Color(255, 105, 180) },
            { new Color(0, 100, 200), new Color(100, 150, 255) },
            { new Color(255, 127, 0), new Color(255, 69, 0) }
    };

    @EventListener(ApplicationReadyEvent.class)
    public void checkAndGenerateMissingCovers() {
        log.info("Starting cover verification and generation process...");

        try {
            Path coversDirectory = Paths.get(coversPath);
            if (!Files.exists(coversDirectory)) {
                Files.createDirectories(coversDirectory);
                log.info("Created covers directory: {}", coversPath);
            }

            List<Track> allTracks = trackRepository.findAll();
            log.info("Found {} tracks in database", allTracks.size());

            int existingCovers = 0;
            int extractedFromFile = 0;
            int downloadedFromItunes = 0;
            int generatedCovers = 0;
            int errorCount = 0;

            for (Track track : allTracks) {
                try {
                    String sanitizedName = track.getFilePath().replaceAll("[^a-zA-Z0-9.-]", "_");
                    Path coversDir = Paths.get(coversPath);
                    boolean foundExisting = false;
                    String foundCoverFile = null;
                    try {
                        if (Files.exists(coversDir) && Files.isDirectory(coversDir)) {
                            try (java.util.stream.Stream<Path> files = Files.list(coversDir)) {
                                java.util.Optional<Path> match = files
                                        .filter(p -> p.getFileName().toString().contains(sanitizedName)).findFirst();
                                if (match.isPresent()) {
                                    foundExisting = true;
                                    foundCoverFile = match.get().getFileName().toString();
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Error searching for existing cover for track {}: {}", track.getFilePath(),
                                e.getMessage());
                    }
                    if (foundExisting) {
                        existingCovers++;
                        String expectedUrl = "/covers/" + foundCoverFile;
                        if (track.getCoverArtUrl() == null || !track.getCoverArtUrl().equals(expectedUrl)) {
                            track.setCoverArtUrl(expectedUrl);
                            trackRepository.save(track);
                            log.debug("Updated cover URL for track {} (existing cover)", track.getFilePath());
                        }
                        continue;
                    }

                    String coverFileName = System.currentTimeMillis() + "_" + sanitizedName + ".jpg";
                    Path coverFilePath = Paths.get(coversPath, coverFileName);
                    String expectedUrl = "/covers/" + coverFileName;
                    boolean coverSet = false;

                    coverSet = trySetCoverFromMetadata(track, coverFilePath, expectedUrl);
                    if (coverSet) {
                        extractedFromFile++;
                        log.info("Extracted embedded artwork for track: {}", track.getFilePath());
                        continue;
                    }

                    coverSet = trySetCoverFromItunes(track, coverFilePath, expectedUrl);
                    if (coverSet) {
                        downloadedFromItunes++;
                        log.info("Downloaded iTunes artwork for track: {} - {}", track.getArtist(), track.getTitle());
                        continue;
                    }

                    coverSet = trySetCoverWithGradient(track, coverFilePath, expectedUrl);
                    if (coverSet) {
                        generatedCovers++;
                        log.info("Generated gradient cover for track: {}", track.getFilePath());
                        continue;
                    }
                } catch (Exception e) {
                    log.error("Error processing cover for track {}: {}", track.getFilePath(), e.getMessage());
                    errorCount++;
                }
            }

            log.info("Cover verification completed:");
            log.info("  - Total tracks: {}", allTracks.size());
            log.info("  - Existing covers: {}", existingCovers);
            log.info("  - Extracted from files: {}", extractedFromFile);
            log.info("  - Downloaded from iTunes: {}", downloadedFromItunes);
            log.info("  - Generated gradients: {}", generatedCovers);
            log.info("  - Errors: {}", errorCount);
        } catch (Exception e) {
            log.error("Error during cover verification process: {}", e.getMessage(), e);
        }
    }

    private boolean trySetCoverFromMetadata(Track track, Path coverFilePath, String expectedUrl) {
        byte[] embeddedArt = extractEmbeddedArtwork(track, musicStoragePath);
        if (embeddedArt != null) {
            try {
                saveAndResizeCover(embeddedArt, coverFilePath);
                track.setCoverArtUrl(expectedUrl);
                trackRepository.save(track);
                return true;
            } catch (Exception e) {
                log.error("Error saving cover from metadata for track {}: {}", track.getFilePath(), e.getMessage());
            }
        } else {
            log.info("No embedded artwork found for track: {} (metaadatból nem sikerült borítót kinyerni)",
                    track.getFilePath());
        }
        return false;
    }

    private boolean trySetCoverFromItunes(Track track, Path coverFilePath, String expectedUrl) {
        byte[] itunesArt = downloadFromItunes(track.getArtist(), track.getTitle());
        if (itunesArt != null) {
            try {
                saveAndResizeCover(itunesArt, coverFilePath);
                track.setCoverArtUrl(expectedUrl);
                trackRepository.save(track);
                return true;
            } catch (Exception e) {
                log.error("Error saving cover from iTunes for track {}: {}", track.getFilePath(), e.getMessage());
            }
        }
        return false;
    }

    private boolean trySetCoverWithGradient(Track track, Path coverFilePath, String expectedUrl) {
        try {
            generateGradientCover(track, coverFilePath);
            track.setCoverArtUrl(expectedUrl);
            trackRepository.save(track);
            return true;
        } catch (Exception e) {
            log.error("Error saving gradient cover for track {}: {}", track.getFilePath(), e.getMessage());
        }
        return false;
    }

    private byte[] extractEmbeddedArtwork(Track track, String musicStoragePath) {
        try {
            Path musicFilePath = Paths.get(musicStoragePath, track.getFilePath());
            if (!Files.exists(musicFilePath)) {
                log.info("Music file not found for embedded artwork extraction: {}", musicFilePath);
                return null;
            }
            AudioFile audioFile = AudioFileIO.read(musicFilePath.toFile());
            Tag tag = audioFile.getTag();
            if (tag == null) {
                log.info("No tag found in file for embedded artwork: {}", musicFilePath);
                return null;
            }
            Artwork artwork = tag.getFirstArtwork();
            if (artwork != null && artwork.getBinaryData() != null) {
                log.info("Embedded artwork found for file: {}", musicFilePath);
                return artwork.getBinaryData();
            } else {
                log.info("No embedded artwork present in tag for file: {}", musicFilePath);
            }
        } catch (Exception e) {
            log.warn("Exception during embedded artwork extraction for {}: {}", track.getFilePath(), e.getMessage());
        }
        return null;
    }

    private byte[] downloadFromItunes(String artist, String title) {
        try {
            String searchTerm = (artist + " " + title).replaceAll(" ", "+");
            String apiUrl = "https://itunes.apple.com/search?term=" + searchTerm + "&limit=1";

            String response = restTemplate.getForObject(apiUrl, String.class);

            if (response == null) {
                return null;
            }

            JsonNode root = objectMapper.readTree(response);
            int resultCount = root.path("resultCount").asInt(0);

            if (resultCount > 0) {
                JsonNode results = root.path("results");
                String artworkUrl = results.get(0).path("artworkUrl100").asText();

                if (artworkUrl != null && !artworkUrl.isEmpty()) {
                    String highResUrl = artworkUrl.replace("100x100", "600x600");

                    if (highResUrl != null && !highResUrl.isEmpty()) {
                        byte[] imageBytes = restTemplate.getForObject(highResUrl, byte[].class);
                        if (imageBytes != null) {
                            return imageBytes;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Could not download iTunes artwork for {} - {}: {}",
                    artist, title, e.getMessage());
        }

        return null;
    }

    private void saveAndResizeCover(byte[] imageBytes, Path outputPath) throws IOException {
        BufferedImage original = ImageIO.read(new ByteArrayInputStream(imageBytes));

        if (original == null) {
            throw new IOException("Could not read image data");
        }

        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();
        double ratio = (double) originalWidth / originalHeight;

        int newWidth, newHeight;
        if (ratio > 1) {
            newWidth = TARGET_SIZE;
            newHeight = (int) (TARGET_SIZE / ratio);
        } else {
            newHeight = TARGET_SIZE;
            newWidth = (int) (TARGET_SIZE * ratio);
        }

        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        BufferedImage finalImage = new BufferedImage(TARGET_SIZE, TARGET_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = finalImage.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, TARGET_SIZE, TARGET_SIZE);

        int x = (TARGET_SIZE - newWidth) / 2;
        int y = (TARGET_SIZE - newHeight) / 2;
        g.drawImage(resized, x, y, null);
        g.dispose();

        ImageIO.write(finalImage, "jpg", outputPath.toFile());
    }

    private void generateGradientCover(Track track, Path outputPath) throws IOException {
        int width = TARGET_SIZE;
        int height = TARGET_SIZE;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        Color[] selectedColors = GRADIENT_COLORS[(int) (Math.random() * GRADIENT_COLORS.length)];

        GradientPaint gradient = new GradientPaint(
                0, 0, selectedColors[0],
                width, height, selectedColors[1]);

        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);

        g2d.setColor(new Color(255, 255, 255, 180));
        int iconSize = 120;
        int iconX = (width - iconSize) / 2;
        int iconY = (height - iconSize) / 2 - 30;

        g2d.fillRoundRect(iconX + 30, iconY + 70, 25, 50, 15, 15);
        g2d.fillOval(iconX + 15, iconY + 100, 40, 40);
        g2d.fillRect(iconX + 50, iconY + 20, 8, 90);
        g2d.fillOval(iconX + 35, iconY + 90, 40, 40);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        FontMetrics fm = g2d.getFontMetrics();

        String title = track.getTitle();
        if (title.length() > 20) {
            title = title.substring(0, 17) + "...";
        }

        int titleWidth = fm.stringWidth(title);
        int titleX = (width - titleWidth) / 2;
        int titleY = height - 80;

        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.drawString(title, titleX + 2, titleY + 2);

        g2d.setColor(Color.WHITE);
        g2d.drawString(title, titleX, titleY);

        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        fm = g2d.getFontMetrics();

        String artist = track.getArtist();
        if (artist.length() > 25) {
            artist = artist.substring(0, 22) + "...";
        }

        int artistWidth = fm.stringWidth(artist);
        int artistX = (width - artistWidth) / 2;
        int artistY = height - 50;

        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.drawString(artist, artistX + 2, artistY + 2);

        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.drawString(artist, artistX, artistY);

        g2d.dispose();

        ImageIO.write(image, "jpg", outputPath.toFile());
    }
}