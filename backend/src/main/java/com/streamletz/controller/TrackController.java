package com.streamletz.controller;

import com.streamletz.model.Track;
import com.streamletz.service.TrackService;
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

import java.util.List;

@RestController
@RequestMapping("/api/tracks")
@RequiredArgsConstructor
@Tag(name = "Tracks", description = "Track management and streaming endpoints")
public class TrackController {

    private final TrackService trackService;

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all tracks", description = "Retrieve list of all available tracks")
    public ResponseEntity<List<Track>> getAllTracks() {
        return ResponseEntity.ok(trackService.getAllTracks());
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get track by ID", description = "Retrieve track metadata by ID")
    public ResponseEntity<Track> getTrackById(@PathVariable Long id) {
        return ResponseEntity.ok(trackService.getTrackById(id));
    }

    @GetMapping("/search")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Search tracks", description = "Search tracks by title, artist, or album")
    public ResponseEntity<List<Track>> searchTracks(@RequestParam String query) {
        return ResponseEntity.ok(trackService.searchTracks(query));
    }

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

    @PostMapping("/{id}/play")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Increment play count", description = "Increment play count when track is played to 90%")
    public ResponseEntity<Void> incrementPlayCount(@PathVariable("id") Long id) {
        System.out.println("Received play count increment request for track: " + id);
        trackService.incrementPlayCount(id);
        return ResponseEntity.ok().build();
    }

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