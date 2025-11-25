package com.streamletz.service;

import com.streamletz.model.Track;
import com.streamletz.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Service for automatically scanning and indexing music files from the file
 * system.
 * 
 * <p>
 * This service implements {@link CommandLineRunner} to execute on application
 * startup
 * when auto-scan is enabled. It performs the following operations:
 * </p>
 * <ul>
 * <li>Recursively scans the configured music directory for audio files</li>
 * <li>Extracts metadata (title, artist, album, duration) from audio files</li>
 * <li>Creates Track entities for new files not already in the database</li>
 * <li>Supports MP3, FLAC, M4A, WAV, and OGG formats</li>
 * </ul>
 * 
 * <p>
 * Scanning depth is limited to 3 levels to prevent excessive recursion.
 * </p>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MusicScannerService implements CommandLineRunner {

    private final TrackRepository trackRepository;

    @Value("${music.storage.path}")
    private String musicStoragePath;

    @Value("${music.auto-scan:true}")
    private boolean autoScan;

    @Value("${music.covers.path:/covers}")
    private String coversPath;

    /**
     * Executes on application startup to scan music library if auto-scan is
     * enabled.
     * 
     * <p>
     * Creates the covers directory if it doesn't exist, then scans the music
     * library
     * and saves any new tracks to the database.
     * </p>
     * 
     * @param args command-line arguments (not used)
     */
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
            java.util.List<Track> scannedTracks = scanMusicLibrary();
            if (!scannedTracks.isEmpty()) {
                trackRepository.saveAll(scannedTracks);
                log.info("Saved {} new tracks to database.", scannedTracks.size());
            } else {
                log.info("No new tracks found to save.");
            }
        }
    }

    /**
     * Scans the music library directory for audio files and extracts metadata.
     * 
     * <p>
     * Recursively scans up to 3 directory levels deep. Skips files already
     * in the database based on file path. Returns only new tracks that need to
     * be saved.
     * </p>
     * 
     * @return list of new Track entities with extracted metadata
     */
    public java.util.List<Track> scanMusicLibrary() {
        java.util.List<Track> scannedTracks = new java.util.ArrayList<>();
        try {
            Path musicDir = Paths.get(musicStoragePath);
            if (!Files.exists(musicDir)) {
                log.info("Creating music directory: {}", musicDir.toAbsolutePath());
                Files.createDirectories(musicDir);
                return scannedTracks;
            }
            for (File file : scanMusicFilesRecursive(musicDir.toFile(), 0, 3)) {
                try {
                    String relativePath = musicDir.toFile().toPath().relativize(file.toPath()).toString().replace('\\',
                            '/');
                    if (trackRepository.findByFilePath(relativePath).isPresent()) {
                        continue;
                    }
                    Track track = extractTrackMetadata(file, relativePath);
                    if (track != null) {
                        scannedTracks.add(track);
                    }
                } catch (Exception e) {
                    log.error("Error processing file {}: {}", file.getName(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error scanning music library: {}", e.getMessage(), e);
        }
        return scannedTracks;
    }

    /**
     * Recursively scans a directory for music files up to a maximum depth.
     * 
     * @param dir          the directory to scan
     * @param currentDepth the current recursion depth
     * @param maxDepth     the maximum recursion depth (3 levels)
     * @return list of music files found
     */
    private java.util.List<File> scanMusicFilesRecursive(File dir, int currentDepth, int maxDepth) {
        java.util.List<File> musicFiles = new java.util.ArrayList<>();
        if (currentDepth > maxDepth || !dir.isDirectory())
            return musicFiles;
        File[] files = dir.listFiles();
        if (files == null)
            return musicFiles;
        for (File file : files) {
            if (file.isDirectory()) {
                if (currentDepth + 1 <= maxDepth) {
                    musicFiles.addAll(scanMusicFilesRecursive(file, currentDepth + 1, maxDepth));
                }
            } else if (isMusicFile(file)) {
                musicFiles.add(file);
            }
        }
        return musicFiles;
    }

    /**
     * Checks if a file is a supported music file based on extension.
     * 
     * <p>
     * Supported formats: MP3, FLAC, M4A, WAV, OGG
     * </p>
     * 
     * @param file the file to check
     * @return true if the file has a supported music file extension
     */
    private boolean isMusicFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".mp3") || name.endsWith(".flac") || name.endsWith(".m4a") || name.endsWith(".wav")
                || name.endsWith(".ogg");
    }

    /**
     * Extracts metadata from an audio file and creates a Track entity.
     * 
     * <p>
     * Uses JAudioTagger library to read ID3 tags and audio properties.
     * Falls back to filename if metadata cannot be read. Sets default values
     * for missing fields ("Unknown Artist", "Unknown Album").
     * </p>
     * 
     * @param file         the audio file to process
     * @param relativePath the relative path from music storage directory
     * @return Track entity with extracted metadata
     * @throws IOException   if file cannot be read
     * @throws TikaException if content type detection fails
     * @throws SAXException  if XML parsing fails
     */
    private Track extractTrackMetadata(File file, String relativePath) throws IOException, TikaException, SAXException {
        Track track = new Track();
        track.setFilePath(relativePath);
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
        } catch (Exception e) {
            log.warn("Could not read metadata for {}: {}, using filename", fileName, e.getMessage());
            track.setTitle(getFileNameWithoutExtension(fileName));
            track.setArtist("Unknown Artist");
        }

        return track;
    }

    /**
     * Removes the file extension from a filename.
     * 
     * @param fileName the filename to process
     * @return the filename without extension
     */
    private String getFileNameWithoutExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(0, lastDot) : fileName;
    }
}