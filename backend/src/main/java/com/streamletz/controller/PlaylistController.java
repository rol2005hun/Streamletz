package com.streamletz.controller;

import com.streamletz.service.PlaylistService;
import com.streamletz.util.dto.CreatePlaylistRequest;
import com.streamletz.util.dto.PlaylistResponse;
import com.streamletz.util.dto.UpdatePlaylistRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for managing user playlists.
 * 
 * <p>
 * This controller provides comprehensive playlist management including:
 * </p>
 * <ul>
 * <li>Creating, reading, updating, and deleting playlists</li>
 * <li>Adding and removing tracks from playlists</li>
 * <li>Reordering tracks within playlists</li>
 * <li>Searching playlists by name or description</li>
 * <li>Managing playlist visibility (public/private)</li>
 * </ul>
 * 
 * <p>
 * All endpoints require JWT authentication. Users can only modify their own
 * playlists but can view public playlists created by others.
 * </p>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
@Tag(name = "Playlists", description = "Playlist management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PlaylistController {

    private final PlaylistService playlistService;

    /**
     * Creates a new playlist for the authenticated user.
     * 
     * <p>
     * The request body is validated for required fields. The playlist is
     * initially private by default unless specified otherwise.
     * </p>
     * 
     * @param request     the playlist creation request containing name,
     *                    description, and visibility
     * @param userDetails the authenticated user's details
     * @return ResponseEntity with HTTP 201 (Created) containing the created
     *         playlist
     */
    @PostMapping
    @Operation(summary = "Create a new playlist")
    public ResponseEntity<PlaylistResponse> createPlaylist(
            @Valid @RequestBody CreatePlaylistRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        PlaylistResponse playlist = playlistService.createPlaylist(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(playlist);
    }

    /**
     * Retrieves all playlists owned by the authenticated user.
     * 
     * @param userDetails the authenticated user's details
     * @return ResponseEntity containing a list of the user's playlists
     */
    @GetMapping
    @Operation(summary = "Get current user's playlists")
    public ResponseEntity<List<PlaylistResponse>> getUserPlaylists(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<PlaylistResponse> playlists = playlistService.getUserPlaylists(userDetails.getUsername());
        return ResponseEntity.ok(playlists);
    }

    /**
     * Retrieves all public playlists visible to all users.
     * 
     * @return ResponseEntity containing a list of public playlists
     */
    @GetMapping("/public")
    @Operation(summary = "Get all public playlists")
    public ResponseEntity<List<PlaylistResponse>> getPublicPlaylists() {
        List<PlaylistResponse> playlists = playlistService.getPublicPlaylists();
        return ResponseEntity.ok(playlists);
    }

    /**
     * Retrieves a specific playlist by ID.
     * 
     * <p>
     * Users can access their own playlists or public playlists created by others.
     * </p>
     * 
     * @param id          the playlist ID
     * @param userDetails the authenticated user's details
     * @return ResponseEntity containing the playlist details
     * @throws RuntimeException if the playlist is not found or not accessible
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get playlist by ID")
    public ResponseEntity<PlaylistResponse> getPlaylistById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        PlaylistResponse playlist = playlistService.getPlaylistById(id, userDetails.getUsername());
        return ResponseEntity.ok(playlist);
    }

    /**
     * Updates an existing playlist's metadata.
     * 
     * <p>
     * Only the playlist owner can update it. The request body is validated
     * for proper format.
     * </p>
     * 
     * @param id          the playlist ID to update
     * @param request     the update request containing new name, description, or
     *                    visibility
     * @param userDetails the authenticated user's details
     * @return ResponseEntity containing the updated playlist
     * @throws RuntimeException if the user is not the owner or playlist not found
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update playlist")
    public ResponseEntity<PlaylistResponse> updatePlaylist(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePlaylistRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        PlaylistResponse playlist = playlistService.updatePlaylist(id, request, userDetails.getUsername());
        return ResponseEntity.ok(playlist);
    }

    /**
     * Deletes a playlist.
     * 
     * <p>
     * Only the playlist owner can delete it.
     * </p>
     * 
     * @param id          the playlist ID to delete
     * @param userDetails the authenticated user's details
     * @return ResponseEntity with HTTP 204 (No Content)
     * @throws RuntimeException if the user is not the owner or playlist not found
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete playlist")
    public ResponseEntity<Void> deletePlaylist(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        playlistService.deletePlaylist(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    /**
     * Adds a track to a playlist.
     * 
     * <p>
     * Only the playlist owner can add tracks. The track is appended to the end
     * of the playlist.
     * </p>
     * 
     * @param id          the playlist ID
     * @param trackId     the track ID to add
     * @param userDetails the authenticated user's details
     * @return ResponseEntity containing the updated playlist
     * @throws RuntimeException if the user is not the owner, playlist not found, or
     *                          track not found
     */
    @PostMapping("/{id}/tracks/{trackId}")
    @Operation(summary = "Add track to playlist")
    public ResponseEntity<PlaylistResponse> addTrackToPlaylist(
            @PathVariable Long id,
            @PathVariable Long trackId,
            @AuthenticationPrincipal UserDetails userDetails) {
        PlaylistResponse playlist = playlistService.addTrackToPlaylist(id, trackId, userDetails.getUsername());
        return ResponseEntity.ok(playlist);
    }

    /**
     * Removes a track from a playlist.
     * 
     * <p>
     * Only the playlist owner can remove tracks.
     * </p>
     * 
     * @param id          the playlist ID
     * @param trackId     the track ID to remove
     * @param userDetails the authenticated user's details
     * @return ResponseEntity containing the updated playlist
     * @throws RuntimeException if the user is not the owner, playlist not found, or
     *                          track not in playlist
     */
    @DeleteMapping("/{id}/tracks/{trackId}")
    @Operation(summary = "Remove track from playlist")
    public ResponseEntity<PlaylistResponse> removeTrackFromPlaylist(
            @PathVariable Long id,
            @PathVariable Long trackId,
            @AuthenticationPrincipal UserDetails userDetails) {
        PlaylistResponse playlist = playlistService.removeTrackFromPlaylist(id, trackId, userDetails.getUsername());
        return ResponseEntity.ok(playlist);
    }

    /**
     * Reorders tracks in a playlist.
     * 
     * <p>
     * Only the playlist owner can reorder tracks. The request body should contain
     * a "trackIds" array with track IDs in the desired order.
     * </p>
     * 
     * @param id          the playlist ID
     * @param request     a map containing "trackIds" key with ordered list of track
     *                    IDs
     * @param userDetails the authenticated user's details
     * @return ResponseEntity containing the updated playlist with reordered tracks
     * @throws RuntimeException if the user is not the owner or playlist not found
     */
    @PutMapping("/{id}/reorder")
    @Operation(summary = "Reorder tracks in playlist")
    public ResponseEntity<PlaylistResponse> reorderTracks(
            @PathVariable Long id,
            @RequestBody Map<String, List<Long>> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<Long> trackIds = request.get("trackIds");
        PlaylistResponse playlist = playlistService.reorderTracks(id, trackIds, userDetails.getUsername());
        return ResponseEntity.ok(playlist);
    }

    /**
     * Searches for playlists by name or description.
     * 
     * <p>
     * Returns playlists that match the query and are either owned by the user
     * or are public. The search is case-insensitive.
     * </p>
     * 
     * @param query       the search query string
     * @param userDetails the authenticated user's details
     * @return ResponseEntity containing a list of matching playlists
     */
    @GetMapping("/search")
    @Operation(summary = "Search playlists")
    public ResponseEntity<List<PlaylistResponse>> searchPlaylists(
            @RequestParam String query,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<PlaylistResponse> playlists = playlistService.searchPlaylists(query, userDetails.getUsername());
        return ResponseEntity.ok(playlists);
    }
}