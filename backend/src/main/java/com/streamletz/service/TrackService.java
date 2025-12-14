package com.streamletz.service;

import com.streamletz.model.Track;
import com.streamletz.repository.TrackRepository;
import com.streamletz.util.dto.TrackListItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for managing music tracks and audio file operations.
 * 
 * <p>
 * This service handles all track-related operations including:
 * </p>
 * <ul>
 * <li>Track metadata retrieval and search</li>
 * <li>Audio file resource loading for streaming</li>
 * <li>Play count tracking and statistics</li>
 * <li>File size and content type determination</li>
 * <li>Track CRUD operations</li>
 * <li>External download integration (placeholder)</li>
 * </ul>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class TrackService {

    private final TrackRepository trackRepository;

    @Value("${music.storage.path}")
    private String musicStoragePath;

    private static final LocalDateTime BROWSE_EPOCH = LocalDateTime.of(1970, 1, 1, 0, 0, 0);

    /**
     * Retrieves all tracks from the database.
     * 
     * @return list of all tracks
     */
    public List<Track> getAllTracks() {
        return trackRepository.findAll();
    }

    public long getTrackCount() {
        return trackRepository.count();
    }

    public List<TrackListItemResponse> browseTracks(int limit, LocalDateTime cursorCreatedAt, Long cursorId) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        var pageable = PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));

        List<Track> tracks;
        if (cursorCreatedAt == null || cursorId == null) {
            tracks = trackRepository.browseFirstPage(BROWSE_EPOCH, pageable);
        } else {
            tracks = trackRepository.browseAfterCursor(BROWSE_EPOCH, cursorCreatedAt, cursorId, pageable);
        }

        return tracks.stream()
                .map(t -> new TrackListItemResponse(
                        t.getId(),
                        t.getTitle(),
                        t.getArtist(),
                        t.getAlbum(),
                        t.getDuration(),
                        t.getCoverArtUrl(),
                t.getFilePath(),
                t.getFileFormat(),
                t.getPlayCount(),
            (t.getCreatedAt() != null ? t.getCreatedAt() : BROWSE_EPOCH)
                ))
                .toList();
    }

    /**
     * Retrieves a specific track by its ID.
     * 
     * @param id the track ID
     * @return the track with the specified ID
     * @throws IllegalArgumentException if id is null
     * @throws RuntimeException         if track is not found
     */
    public Track getTrackById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Track ID cannot be null");
        }
        return trackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Track not found with id: " + id));
    }

    /**
     * Searches for tracks by title, artist, or album.
     * 
     * <p>
     * Performs case-insensitive search across track titles, artist names,
     * and album names. Returns all matching tracks without duplicates.
     * </p>
     * 
     * @param query the search query string
     * @return list of tracks matching the query
     */
    public List<Track> searchTracks(String query) {
        List<Track> tracks = trackRepository.findByTitleContainingIgnoreCase(query);
        tracks.addAll(trackRepository.findByArtistContainingIgnoreCase(query));
        tracks.addAll(trackRepository.findByAlbumContainingIgnoreCase(query));
        return tracks.stream().distinct().toList();
    }

    /**
     * Loads the audio file resource for streaming.
     * 
     * <p>
     * Resolves the track's file path and creates a {@link Resource} object
     * for streaming the audio content. Validates that the file exists and is
     * readable.
     * </p>
     * 
     * @param trackId the ID of the track to stream
     * @return Resource object for the audio file
     * @throws RuntimeException if track not found, file not found, or file not
     *                          readable
     */
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

    /**
     * Increments the play count for a track.
     * 
     * <p>
     * Should be called when a track has been played to approximately 90%
     * completion to count as a valid play for statistics.
     * </p>
     * 
     * @param trackId the ID of the track
     */
    public void incrementPlayCount(Long trackId) {
        Track track = getTrackById(trackId);
        Integer currentPlayCount = track.getPlayCount();
        track.setPlayCount(currentPlayCount != null ? currentPlayCount + 1 : 1);
        trackRepository.save(track);
    }

    /**
     * Gets the file size of a track's audio file.
     * 
     * @param trackId the ID of the track
     * @return the file size in bytes
     * @throws RuntimeException if file size cannot be determined
     */
    public long getTrackFileSize(Long trackId) {
        Track track = getTrackById(trackId);
        try {
            Path filePath = Paths.get(musicStoragePath).resolve(track.getFilePath()).normalize();
            return Files.size(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error getting file size: " + e.getMessage());
        }
    }

    /**
     * Determines the MIME content type for a track based on its file format.
     * 
     * <p>
     * Maps file extensions to appropriate MIME types for HTTP streaming.
     * Defaults to "audio/mpeg" if format is unknown.
     * </p>
     * 
     * @param trackId the ID of the track
     * @return the MIME content type string
     */
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

    /**
     * Saves a track to the database.
     * 
     * @param track the track to save
     * @return the saved track with generated ID
     * @throws IllegalArgumentException if track is null
     */
    public Track saveTrack(Track track) {
        if (track == null) {
            throw new IllegalArgumentException("Track cannot be null");
        }
        return trackRepository.save(track);
    }

    /**
     * Deletes a track from the database.
     * 
     * @param id the ID of the track to delete
     * @throws IllegalArgumentException if id is null
     */
    public void deleteTrack(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Track ID cannot be null");
        }
        trackRepository.deleteById(id);
    }

    /**
     * Downloads a track from an external source (placeholder).
     * 
     * <p>
     * <b>Note:</b> This feature is not yet implemented and will throw
     * {@link UnsupportedOperationException}.
     * </p>
     * 
     * @param source the external source (e.g., "youtube", "spotify")
     * @param url    the URL of the track to download
     * @throws UnsupportedOperationException always, as feature is not implemented
     */
    public void downloadTrackFromExternal(String source, String url) {
        // TODO: Implement YouTube/Spotify download integration
        throw new UnsupportedOperationException("Download feature not yet implemented");
    }
}