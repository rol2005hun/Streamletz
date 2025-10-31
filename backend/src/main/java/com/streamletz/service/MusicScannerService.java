package com.streamletz.service;

import com.streamletz.model.Track;
import com.streamletz.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.metadata.XMPDM;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
@Slf4j
public class MusicScannerService implements CommandLineRunner {

    private final TrackRepository trackRepository;

    @Value("${music.storage.path}")
    private String musicStoragePath;

    @Value("${music.auto-scan:true}")
    private boolean autoScan;

    @Value("${music.covers.path:./covers}")
    private String coversPath;

    @Override
    public void run(String... args) {
        if (autoScan) {
            log.info("Starting automatic music library scan...");
            // Create covers directory if it doesn't exist
            try {
                Path coversDir = Paths.get(coversPath);
                log.info("Checking covers directory: {}", coversDir.toAbsolutePath());
                if (!Files.exists(coversDir)) {
                    Files.createDirectories(coversDir);
                    log.info("Created covers directory: {}", coversDir.toAbsolutePath());
                } else {
                    log.info("Covers directory already exists: {}", coversDir.toAbsolutePath());
                }
            } catch (IOException e) {
                log.error("Failed to create covers directory: {}", e.getMessage());
            }
            scanMusicLibrary();
        }
    }

    public void scanMusicLibrary() {
        try {
            Path musicDir = Paths.get(musicStoragePath);

            if (!Files.exists(musicDir)) {
                log.info("Creating music directory: {}", musicDir.toAbsolutePath());
                Files.createDirectories(musicDir);
                return;
            }

            File[] musicFiles = musicDir.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3") ||
                    name.toLowerCase().endsWith(".flac") ||
                    name.toLowerCase().endsWith(".m4a") ||
                    name.toLowerCase().endsWith(".wav") ||
                    name.toLowerCase().endsWith(".ogg"));

            if (musicFiles == null || musicFiles.length == 0) {
                log.info("No music files found in {}", musicDir.toAbsolutePath());
                return;
            }

            log.info("Found {} music files", musicFiles.length);

            int added = 0;
            int skipped = 0;

            for (File file : musicFiles) {
                try {
                    String fileName = file.getName();

                    // Check if track already exists
                    if (trackRepository.findByFilePath(fileName).isPresent()) {
                        skipped++;
                        continue;
                    }

                    Track track = extractTrackMetadata(file);
                    if (track != null) {
                        trackRepository.save(track);
                        added++;
                        log.info("Added track: {} - {}", track.getArtist(), track.getTitle());
                    }
                } catch (Exception e) {
                    log.error("Error processing file {}: {}", file.getName(), e.getMessage());
                }
            }

            log.info("Music scan complete. Added: {}, Skipped: {}", added, skipped);

        } catch (Exception e) {
            log.error("Error scanning music library: {}", e.getMessage(), e);
        }
    }

    private Track extractTrackMetadata(File file) throws IOException, TikaException, SAXException {
        Track track = new Track();
        track.setFilePath(file.getName());
        track.setFileSize(file.length());
        track.setPlayCount(0);

        // Determine file format
        String fileName = file.getName();
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        track.setFileFormat(extension);

        try (FileInputStream inputstream = new FileInputStream(file)) {
            BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            Parser parser = new Mp3Parser();
            ParseContext context = new ParseContext();

            parser.parse(inputstream, handler, metadata, context);

            // Extract title
            String title = metadata.get(TikaCoreProperties.TITLE);
            track.setTitle(title != null && !title.isEmpty() ? title : getFileNameWithoutExtension(fileName));

            // Extract artist
            String artist = metadata.get(TikaCoreProperties.CREATOR);
            if (artist == null || artist.isEmpty()) {
                artist = metadata.get("xmpDM:artist");
            }
            track.setArtist(artist != null && !artist.isEmpty() ? artist : "Unknown Artist");

            // Extract album
            String album = metadata.get(XMPDM.ALBUM);
            track.setAlbum(album != null && !album.isEmpty() ? album : null);

            // Extract duration (in seconds)
            String durationStr = metadata.get(XMPDM.DURATION);
            if (durationStr != null) {
                try {
                    // Duration might be in milliseconds, convert to seconds
                    double durationMs = Double.parseDouble(durationStr);
                    track.setDuration((int) (durationMs / 1000));
                } catch (NumberFormatException e) {
                    log.warn("Could not parse duration for {}", fileName);
                }
            }

            // Extract cover art
            extractAndSaveCoverArt(file, metadata, track);

        } catch (Exception e) {
            // If metadata reading fails, use filename
            log.warn("Could not read metadata for {}, using filename", fileName);
            track.setTitle(getFileNameWithoutExtension(fileName));
            track.setArtist("Unknown Artist");
        }

        return track;
    }

    private String getFileNameWithoutExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(0, lastDot) : fileName;
    }

    private void extractAndSaveCoverArt(File file, Metadata metadata, Track track) {
        try {
            log.debug("Attempting to extract cover art from {}", file.getName());
            // Use JAudiotagger to extract cover art
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();

            if (tag != null) {
                log.debug("Tag found for {}", file.getName());
                var artwork = tag.getFirstArtwork();
                if (artwork != null) {
                    log.debug("Artwork found for {}", file.getName());
                    byte[] imageData = artwork.getBinaryData();
                    if (imageData != null && imageData.length > 0) {
                        log.info("Extracting cover art ({} bytes) from {}", imageData.length, file.getName());
                        saveCoverImage(imageData, track);
                        return;
                    } else {
                        log.debug("Artwork binary data is null or empty for {}", file.getName());
                    }
                } else {
                    log.debug("No artwork found in tag for {}", file.getName());
                }
            } else {
                log.debug("No tag found for {}", file.getName());
            }

            log.debug("No cover art found in {}", file.getName());
        } catch (Exception e) {
            log.warn("Failed to extract cover art from {}: {}", file.getName(), e.getMessage());
        }
    }

    private void saveCoverImage(byte[] imageData, Track track) {
        try {
            // Generate unique filename for cover
            String coverFileName = System.currentTimeMillis() + "_" +
                    track.getFilePath().replaceAll("[^a-zA-Z0-9.-]", "_") + ".jpg";
            Path coverPath = Paths.get(coversPath, coverFileName);

            // Save image file
            try (FileOutputStream fos = new FileOutputStream(coverPath.toFile())) {
                fos.write(imageData);
            }

            // Set cover URL (relative path for API)
            track.setCoverArtUrl("/api/covers/" + coverFileName);
            log.info("Saved cover art: {}", coverFileName);
        } catch (IOException e) {
            log.error("Failed to save cover image: {}", e.getMessage());
        }
    }
}