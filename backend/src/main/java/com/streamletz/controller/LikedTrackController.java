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

/**
 * REST controller for managing user's liked tracks.
 * 
 * <p>
 * This controller provides endpoints for:
 * </p>
 * <ul>
 * <li>Liking and unliking tracks</li>
 * <li>Retrieving all liked tracks for a user</li>
 * <li>Checking if a specific track is liked</li>
 * <li>Getting the total count of liked tracks</li>
 * </ul>
 * 
 * <p>
 * All endpoints require JWT authentication and operate on the authenticated
 * user's liked tracks collection.
 * </p>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/liked")
@RequiredArgsConstructor
@Tag(name = "Liked Tracks", description = "Liked tracks management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class LikedTrackController {

    private final LikedTrackService likedTrackService;

    /**
     * Adds a track to the user's liked tracks collection.
     * 
     * <p>
     * If the track is already liked, this operation has no effect.
     * </p>
     * 
     * @param trackId     the ID of the track to like
     * @param userDetails the authenticated user's details
     * @return ResponseEntity with success message
     * @throws RuntimeException if the track is not found
     */
    @PostMapping("/tracks/{trackId}")
    @Operation(summary = "Like a track")
    public ResponseEntity<Map<String, String>> likeTrack(
            @PathVariable Long trackId,
            @AuthenticationPrincipal UserDetails userDetails) {
        likedTrackService.likeTrack(trackId, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Track liked successfully"));
    }

    /**
     * Removes a track from the user's liked tracks collection.
     * 
     * @param trackId     the ID of the track to unlike
     * @param userDetails the authenticated user's details
     * @return ResponseEntity with success message
     * @throws RuntimeException if the track is not found or not liked
     */
    @DeleteMapping("/tracks/{trackId}")
    @Operation(summary = "Unlike a track")
    public ResponseEntity<Map<String, String>> unlikeTrack(
            @PathVariable Long trackId,
            @AuthenticationPrincipal UserDetails userDetails) {
        likedTrackService.unlikeTrack(trackId, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Track unliked successfully"));
    }

    /**
     * Retrieves all tracks liked by the authenticated user.
     * 
     * <p>
     * Returns tracks ordered by when they were liked, most recent first.
     * </p>
     * 
     * @param userDetails the authenticated user's details
     * @return ResponseEntity containing a list of liked tracks
     */
    @GetMapping("/tracks")
    @Operation(summary = "Get all liked tracks")
    public ResponseEntity<List<Track>> getLikedTracks(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<Track> tracks = likedTrackService.getLikedTracks(userDetails.getUsername());
        return ResponseEntity.ok(tracks);
    }

    /**
     * Checks if a specific track is liked by the authenticated user.
     * 
     * @param trackId     the ID of the track to check
     * @param userDetails the authenticated user's details
     * @return ResponseEntity containing a map with "isLiked" boolean value
     */
    @GetMapping("/tracks/{trackId}/status")
    @Operation(summary = "Check if track is liked")
    public ResponseEntity<Map<String, Boolean>> isTrackLiked(
            @PathVariable Long trackId,
            @AuthenticationPrincipal UserDetails userDetails) {
        boolean isLiked = likedTrackService.isTrackLiked(trackId, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("isLiked", isLiked));
    }

    /**
     * Gets the total count of tracks liked by the authenticated user.
     * 
     * @param userDetails the authenticated user's details
     * @return ResponseEntity containing a map with "count" long value
     */
    @GetMapping("/tracks/count")
    @Operation(summary = "Get liked tracks count")
    public ResponseEntity<Map<String, Long>> getLikedTracksCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        long count = likedTrackService.getLikedTracksCount(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("count", count));
    }
}
