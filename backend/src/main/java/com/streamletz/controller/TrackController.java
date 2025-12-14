package com.streamletz.controller;

import com.streamletz.model.Track;
import com.streamletz.service.TrackService;
import com.streamletz.util.dto.TrackBrowseResponse;
import com.streamletz.util.dto.TrackListItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing music tracks and audio streaming.
 * 
 * <p>
 * This controller provides endpoints for:
 * </p>
 * <ul>
 * <li>Track metadata retrieval and search</li>
 * <li>Audio streaming with HTTP Range support for seeking</li>
 * <li>Play count tracking</li>
 * <li>External track download (placeholder for future implementation)</li>
 * </ul>
 * 
 * <p>
 * The streaming endpoint supports partial content requests (HTTP 206) for
 * efficient audio playback and seeking.
 * </p>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/tracks")
@RequiredArgsConstructor
@Tag(name = "Tracks", description = "Track management and streaming endpoints")
public class TrackController {

    private final TrackService trackService;

    /**
     * Retrieves all available tracks.
     * 
     * <p>
     * Returns a list of all tracks in the system with their metadata.
     * Requires authentication.
     * </p>
     * 
     * @return ResponseEntity containing a list of all tracks
     */
    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all tracks", description = "Retrieve list of all available tracks")
    public ResponseEntity<List<Track>> getAllTracks() {
        return ResponseEntity.ok(trackService.getAllTracks());
    }

    @GetMapping("/count")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get track count", description = "Get total number of tracks")
    public ResponseEntity<Map<String, Long>> getTrackCount() {
        return ResponseEntity.ok(Map.of("count", trackService.getTrackCount()));
    }

    /**
     * Paginated browse endpoint using keyset cursor (createdAt,id).
     */
    @GetMapping("/browse")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Browse tracks", description = "Browse tracks with cursor-based pagination")
    public ResponseEntity<TrackBrowseResponse> browseTracks(
            @RequestParam(name = "limit", defaultValue = "50") int limit,
            @RequestParam(name = "cursor", required = false) String cursor) {

        CursorParts cursorParts = decodeCursor(cursor);
        List<TrackListItemResponse> items = trackService.browseTracks(limit, cursorParts.createdAt, cursorParts.id);

        // hasMore: if we received 'limit' items, assume there may be more.
        // (We intentionally avoid extra DB calls; frontend can request next page.)
        boolean hasMore = items.size() >= Math.max(1, Math.min(limit, 100));
        String nextCursor = null;
        if (!items.isEmpty() && hasMore) {
            TrackListItemResponse last = items.get(items.size() - 1);
            if (last.getCreatedAt() != null && last.getId() != null) {
                nextCursor = encodeCursor(last.getCreatedAt(), last.getId());
            }
        }

        return ResponseEntity.ok(new TrackBrowseResponse(items, nextCursor, hasMore));
    }

    /**
     * Jump-to-page browse endpoint.
     *
     * <p>
     * This is intended for scrollbar seeking: it returns the requested page in the
     * same sort order as {@link #browseTracks(int, String)}.
     * </p>
     */
    @GetMapping("/browse/page")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Browse tracks at page", description = "Browse tracks by page number (for scrollbar seeking)")
    public ResponseEntity<TrackBrowseResponse> browseTracksPage(
            @RequestParam(name = "limit", defaultValue = "50") int limit,
            @RequestParam(name = "page", defaultValue = "0") int page) {

        int safeLimit = Math.max(1, Math.min(limit, 100));
        int safePage = Math.max(0, page);

        List<TrackListItemResponse> items = trackService.browseTracksPage(safeLimit, safePage);

        boolean hasMore = items.size() >= safeLimit;
        String nextCursor = null;
        if (!items.isEmpty() && hasMore) {
            TrackListItemResponse last = items.get(items.size() - 1);
            if (last.getCreatedAt() != null && last.getId() != null) {
                nextCursor = encodeCursor(last.getCreatedAt(), last.getId());
            }
        }

        return ResponseEntity.ok(new TrackBrowseResponse(items, nextCursor, hasMore));
    }

    private static final DateTimeFormatter CURSOR_TIME_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private record CursorParts(LocalDateTime createdAt, Long id) {
    }

    private CursorParts decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return new CursorParts(null, null);
        }
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = decoded.split("\\|", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid cursor format");
            }
            LocalDateTime createdAt = LocalDateTime.parse(parts[0], CURSOR_TIME_FORMAT);
            Long id = Long.parseLong(parts[1]);
            return new CursorParts(createdAt, id);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor");
        }
    }

    private String encodeCursor(LocalDateTime createdAt, Long id) {
        String raw = createdAt.format(CURSOR_TIME_FORMAT) + "|" + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Retrieves a specific track by its ID.
     * 
     * @param id the track ID
     * @return ResponseEntity containing the track metadata
     * @throws RuntimeException if the track is not found
     */
    @GetMapping("/{id:\\d+}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get track by ID", description = "Retrieve track metadata by ID")
    public ResponseEntity<Track> getTrackById(@PathVariable Long id) {
        return ResponseEntity.ok(trackService.getTrackById(id));
    }

    /**
     * Searches for tracks by title, artist, or album.
     * 
     * <p>
     * Performs a case-insensitive search across track titles, artist names,
     * and album names. Returns all matching tracks without duplicates.
     * </p>
     * 
     * @param query the search query string
     * @return ResponseEntity containing a list of matching tracks
     */
    @GetMapping("/search")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Search tracks", description = "Search tracks by title, artist, or album")
    public ResponseEntity<List<Track>> searchTracks(@RequestParam String query) {
        return ResponseEntity.ok(trackService.searchTracks(query));
    }

    /**
     * Streams audio content with support for HTTP Range requests.
     * 
     * <p>
     * This endpoint supports partial content delivery (HTTP 206) for efficient
     * audio streaming and seeking. If a Range header is provided, only the
     * requested
     * byte range is returned. Otherwise, the entire file is streamed.
     * </p>
     * 
     * <p>
     * The endpoint is publicly accessible to allow audio playback without
     * authentication.
     * </p>
     * 
     * @param id          the track ID to stream
     * @param rangeHeader optional HTTP Range header for partial content requests
     * @return ResponseEntity containing the audio resource with appropriate headers
     */
    @GetMapping("/stream/{id}")
    @Operation(summary = "Stream track", description = "Stream audio with HTTP Range support")
    public ResponseEntity<Resource> streamTrack(
            @PathVariable Long id,
            @RequestHeader(value = "Range", required = false) String rangeHeader) {

        try {
            Resource resource = trackService.getTrackResource(id);
            long fileSize = trackService.getTrackFileSize(id);
            String contentType = trackService.getContentType(id);
            if (contentType == null || contentType.isEmpty()) {
                contentType = "audio/mpeg";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");

            if (rangeHeader == null || rangeHeader.isEmpty()) {
                headers.setContentLength(fileSize);
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(resource);
            } else {
                List<HttpRange> ranges = HttpRange.parseRanges(rangeHeader);
                HttpRange range = ranges.get(0);

                long start = range.getRangeStart(fileSize);
                long end = range.getRangeEnd(fileSize);
                long contentLength = end - start + 1;

                headers.add(HttpHeaders.CONTENT_RANGE,
                        String.format("bytes %d-%d/%d", start, end, fileSize));
                headers.setContentLength(contentLength);

                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .headers(headers)
                        .body(resource);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
     * @param id the track ID
     * @return ResponseEntity with no content
     */
    @PostMapping("/{id}/play")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Increment play count", description = "Increment play count when track is played to 90%")
    public ResponseEntity<Void> incrementPlayCount(@PathVariable("id") Long id) {
        System.out.println("Received play count increment request for track: " + id);
        trackService.incrementPlayCount(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Initiates download of a track from an external source.
     * 
     * <p>
     * <b>Note:</b> This is a placeholder endpoint for future implementation.
     * Currently returns HTTP 501 (Not Implemented).
     * </p>
     * 
     * @param source the external source (e.g., "youtube", "spotify")
     * @param url    the URL of the track to download
     * @return ResponseEntity with status message
     * @throws UnsupportedOperationException always, as feature is not yet
     *                                       implemented
     */
    @PostMapping("/download")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Download track from external source", description = "Trigger download from YouTube or Spotify (placeholder)")
    public ResponseEntity<String> downloadFromExternal(
            @RequestParam String source,
            @RequestParam String url) {
        try {
            trackService.downloadTrackFromExternal(source, url);
            return ResponseEntity.ok("Download initiated");
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body("Feature not yet implemented");
        }
    }
}