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
                    String fileName = file.getName();
                    if (trackRepository.findByFilePath(fileName).isPresent()) {
                        continue;
                    }
                    Track track = extractTrackMetadata(file);
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

    private boolean isMusicFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".mp3") || name.endsWith(".flac") || name.endsWith(".m4a") || name.endsWith(".wav")
                || name.endsWith(".ogg");
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
}