package com.streamletz.controller;

import com.streamletz.model.Track;
import com.streamletz.service.LikedTrackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/liked")
@RequiredArgsConstructor
@Tag(name = "Liked Tracks", description = "Liked tracks management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class LikedTrackController {

    private final LikedTrackService likedTrackService;

    @PostMapping("/tracks/{trackId}")
    @Operation(summary = "Like a track")
    public ResponseEntity<Map<String, String>> likeTrack(
            @PathVariable Long trackId,
            @AuthenticationPrincipal UserDetails userDetails) {
        likedTrackService.likeTrack(trackId, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Track liked successfully"));
    }

    @DeleteMapping("/tracks/{trackId}")
    @Operation(summary = "Unlike a track")
    public ResponseEntity<Map<String, String>> unlikeTrack(
            @PathVariable Long trackId,
            @AuthenticationPrincipal UserDetails userDetails) {
        likedTrackService.unlikeTrack(trackId, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Track unliked successfully"));
    }

    @GetMapping("/tracks")
    @Operation(summary = "Get all liked tracks")
    public ResponseEntity<List<Track>> getLikedTracks(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<Track> tracks = likedTrackService.getLikedTracks(userDetails.getUsername());
        return ResponseEntity.ok(tracks);
    }

    @GetMapping("/tracks/{trackId}/status")
    @Operation(summary = "Check if track is liked")
    public ResponseEntity<Map<String, Boolean>> isTrackLiked(
            @PathVariable Long trackId,
            @AuthenticationPrincipal UserDetails userDetails) {
        boolean isLiked = likedTrackService.isTrackLiked(trackId, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("isLiked", isLiked));
    }

    @GetMapping("/tracks/count")
    @Operation(summary = "Get liked tracks count")
    public ResponseEntity<Map<String, Long>> getLikedTracksCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        long count = likedTrackService.getLikedTracksCount(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("count", count));
    }
}
