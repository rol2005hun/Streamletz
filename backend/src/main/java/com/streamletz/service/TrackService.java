package com.streamletz.service;

import com.streamletz.model.Track;
import com.streamletz.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrackService {

    private final TrackRepository trackRepository;

    @Value("${music.storage.path}")
    private String musicStoragePath;

    public List<Track> getAllTracks() {
        return trackRepository.findAll();
    }

    public Track getTrackById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Track ID cannot be null");
        }
        return trackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Track not found with id: " + id));
    }

    public List<Track> searchTracks(String query) {
        List<Track> tracks = trackRepository.findByTitleContainingIgnoreCase(query);
        tracks.addAll(trackRepository.findByArtistContainingIgnoreCase(query));
        tracks.addAll(trackRepository.findByAlbumContainingIgnoreCase(query));
        return tracks.stream().distinct().toList();
    }

    public Resource getTrackResource(Long trackId) {
        Track track = getTrackById(trackId);
        try {
            Path filePath = Paths.get(musicStoragePath).resolve(track.getFilePath()).normalize();
            java.net.URI uri = filePath.toUri();
            if (uri == null) {
                throw new RuntimeException("Could not create URI for file: " + track.getFilePath());
            }
            Resource resource = new UrlResource(uri);

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found or not readable: " + track.getFilePath());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error loading track file: " + e.getMessage());
        }
    }

    public void incrementPlayCount(Long trackId) {
        Track track = getTrackById(trackId);
        Integer currentPlayCount = track.getPlayCount();
        track.setPlayCount(currentPlayCount != null ? currentPlayCount + 1 : 1);
        trackRepository.save(track);
    }

    public long getTrackFileSize(Long trackId) {
        Track track = getTrackById(trackId);
        try {
            Path filePath = Paths.get(musicStoragePath).resolve(track.getFilePath()).normalize();
            return Files.size(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error getting file size: " + e.getMessage());
        }
    }

    public String getContentType(Long trackId) {
        Track track = getTrackById(trackId);
        String format = track.getFileFormat();

        if (format == null) {
            return "audio/mpeg";
        }

        return switch (format.toLowerCase()) {
            case "mp3" -> "audio/mpeg";
            case "wav" -> "audio/wav";
            case "flac" -> "audio/flac";
            case "m4a" -> "audio/mp4";
            case "ogg" -> "audio/ogg";
            default -> "audio/mpeg";
        };
    }

    public Track saveTrack(Track track) {
        if (track == null) {
            throw new IllegalArgumentException("Track cannot be null");
        }
        return trackRepository.save(track);
    }

    public void deleteTrack(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Track ID cannot be null");
        }
        trackRepository.deleteById(id);
    }

    public void downloadTrackFromExternal(String source, String url) {
        // TODO: Implement YouTube/Spotify download integration
        throw new UnsupportedOperationException("Download feature not yet implemented");
    }
}