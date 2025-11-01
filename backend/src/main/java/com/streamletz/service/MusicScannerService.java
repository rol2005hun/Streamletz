package com.streamletz.service;

import com.streamletz.model.Track;
import com.streamletz.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.File;
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

        String fileName = file.getName();
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        track.setFileFormat(extension);

        try {
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();

            if (tag != null) {
                String title = tag.getFirst(org.jaudiotagger.tag.FieldKey.TITLE);
                track.setTitle(title != null && !title.isEmpty() ? title : getFileNameWithoutExtension(fileName));

                String artist = tag.getFirst(org.jaudiotagger.tag.FieldKey.ARTIST);
                track.setArtist(artist != null && !artist.isEmpty() ? artist : "Unknown Artist");

                String album = tag.getFirst(org.jaudiotagger.tag.FieldKey.ALBUM);
                log.debug("Album value for {}: '{}' (length: {}, is null: {}, is empty: {})",
                        fileName, album, album != null ? album.length() : -1, album == null,
                        album != null && album.isEmpty());
                track.setAlbum(album != null && !album.isEmpty() && !"0".equals(album) ? album : "Unknown Album");
            } else {
                track.setTitle(getFileNameWithoutExtension(fileName));
                track.setArtist("Unknown Artist");
                track.setAlbum("Unknown Album");
            }

            int durationInSeconds = audioFile.getAudioHeader().getTrackLength();
            track.setDuration(durationInSeconds);
            log.debug("Duration for {}: {} seconds", fileName, durationInSeconds);

            extractAndSaveCoverArt(file, null, track);

        } catch (Exception e) {
            log.warn("Could not read metadata for {}: {}, using filename", fileName, e.getMessage());
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
            String coverFileName = System.currentTimeMillis() + "_" +
                    track.getFilePath().replaceAll("[^a-zA-Z0-9.-]", "_") + ".jpg";
            Path coverPath = Paths.get(coversPath, coverFileName);

            try (FileOutputStream fos = new FileOutputStream(coverPath.toFile())) {
                fos.write(imageData);
            }

            track.setCoverArtUrl("/api/covers/" + coverFileName);
            log.info("Saved cover art: {}", coverFileName);
        } catch (IOException e) {
            log.error("Failed to save cover image: {}", e.getMessage());
        }
    }
}