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

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
@Tag(name = "Playlists", description = "Playlist management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PlaylistController {

    private final PlaylistService playlistService;

    @PostMapping
    @Operation(summary = "Create a new playlist")
    public ResponseEntity<PlaylistResponse> createPlaylist(
            @Valid @RequestBody CreatePlaylistRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        PlaylistResponse playlist = playlistService.createPlaylist(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(playlist);
    }

    @GetMapping
    @Operation(summary = "Get current user's playlists")
    public ResponseEntity<List<PlaylistResponse>> getUserPlaylists(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<PlaylistResponse> playlists = playlistService.getUserPlaylists(userDetails.getUsername());
        return ResponseEntity.ok(playlists);
    }

    @GetMapping("/public")
    @Operation(summary = "Get all public playlists")
    public ResponseEntity<List<PlaylistResponse>> getPublicPlaylists() {
        List<PlaylistResponse> playlists = playlistService.getPublicPlaylists();
        return ResponseEntity.ok(playlists);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get playlist by ID")
    public ResponseEntity<PlaylistResponse> getPlaylistById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        PlaylistResponse playlist = playlistService.getPlaylistById(id, userDetails.getUsername());
        return ResponseEntity.ok(playlist);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update playlist")
    public ResponseEntity<PlaylistResponse> updatePlaylist(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePlaylistRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        PlaylistResponse playlist = playlistService.updatePlaylist(id, request, userDetails.getUsername());
        return ResponseEntity.ok(playlist);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete playlist")
    public ResponseEntity<Void> deletePlaylist(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        playlistService.deletePlaylist(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/tracks/{trackId}")
    @Operation(summary = "Add track to playlist")
    public ResponseEntity<PlaylistResponse> addTrackToPlaylist(
            @PathVariable Long id,
            @PathVariable Long trackId,
            @AuthenticationPrincipal UserDetails userDetails) {
        PlaylistResponse playlist = playlistService.addTrackToPlaylist(id, trackId, userDetails.getUsername());
        return ResponseEntity.ok(playlist);
    }

    @DeleteMapping("/{id}/tracks/{trackId}")
    @Operation(summary = "Remove track from playlist")
    public ResponseEntity<PlaylistResponse> removeTrackFromPlaylist(
            @PathVariable Long id,
            @PathVariable Long trackId,
            @AuthenticationPrincipal UserDetails userDetails) {
        PlaylistResponse playlist = playlistService.removeTrackFromPlaylist(id, trackId, userDetails.getUsername());
        return ResponseEntity.ok(playlist);
    }

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

    @GetMapping("/search")
    @Operation(summary = "Search playlists")
    public ResponseEntity<List<PlaylistResponse>> searchPlaylists(
            @RequestParam String query,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<PlaylistResponse> playlists = playlistService.searchPlaylists(query, userDetails.getUsername());
        return ResponseEntity.ok(playlists);
    }
}
