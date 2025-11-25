package com.streamletz.service;

import com.streamletz.model.Playlist;
import com.streamletz.model.Track;
import com.streamletz.model.User;
import com.streamletz.repository.PlaylistRepository;
import com.streamletz.repository.TrackRepository;
import com.streamletz.repository.UserRepository;
import com.streamletz.util.dto.CreatePlaylistRequest;
import com.streamletz.util.dto.PlaylistResponse;
import com.streamletz.util.dto.UpdatePlaylistRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing user playlists.
 * 
 * <p>
 * This service handles all playlist-related operations including:
 * </p>
 * <ul>
 * <li>Creating, reading, updating, and deleting playlists</li>
 * <li>Managing playlist tracks (add, remove, reorder)</li>
 * <li>Searching playlists by name or description</li>
 * <li>Access control (public/private playlists, owner verification)</li>
 * <li>Converting playlist entities to response DTOs</li>
 * </ul>
 * 
 * <p>
 * All operations are transactional to ensure data consistency.
 * </p>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class PlaylistService {

        private final PlaylistRepository playlistRepository;
        private final TrackRepository trackRepository;
        private final UserRepository userRepository;

        /**
         * Creates a new playlist for the specified user.
         * 
         * <p>
         * Playlists are private by default unless explicitly set to public.
         * </p>
         * 
         * @param request  the playlist creation request
         * @param username the owner's username
         * @return PlaylistResponse with the created playlist details
         * @throws RuntimeException if user is not found
         */
        @Transactional
        public PlaylistResponse createPlaylist(CreatePlaylistRequest request, String username) {
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Playlist playlist = new Playlist();
                playlist.setName(request.getName());
                playlist.setDescription(request.getDescription());
                playlist.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : false);
                playlist.setOwner(user);

                Playlist saved = playlistRepository.save(playlist);
                return convertToResponse(saved, false);
        }

        /**
         * Retrieves all playlists owned by the specified user.
         * 
         * <p>
         * Returns playlists ordered by creation date, most recent first.
         * </p>
         * 
         * @param username the owner's username
         * @return list of user's playlists
         * @throws RuntimeException if user is not found
         */
        @Transactional(readOnly = true)
        public List<PlaylistResponse> getUserPlaylists(String username) {
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("User not found"));
                return playlistRepository.findByOwnerOrderByCreatedAtDesc(user).stream()
                                .map(playlist -> convertToResponse(playlist, false))
                                .collect(Collectors.toList());
        }

        /**
         * Retrieves all public playlists visible to all users.
         * 
         * @return list of public playlists
         */
        @Transactional(readOnly = true)
        public List<PlaylistResponse> getPublicPlaylists() {
                return playlistRepository.findByIsPublicTrue().stream()
                                .map(playlist -> convertToResponse(playlist, false))
                                .collect(Collectors.toList());
        }

        /**
         * Retrieves a specific playlist by ID with access control.
         * 
         * <p>
         * Users can access their own playlists or public playlists.
         * Private playlists owned by others are not accessible.
         * </p>
         * 
         * @param id       the playlist ID
         * @param username the requesting user's username
         * @return PlaylistResponse with full track details
         * @throws RuntimeException if playlist not found, user not found, or access
         *                          denied
         */
        @Transactional(readOnly = true)
        public PlaylistResponse getPlaylistById(Long id, String username) {
                if (id == null) {
                        throw new RuntimeException("Playlist ID cannot be null");
                }

                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Playlist playlist = playlistRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Playlist not found"));

                if (!playlist.getIsPublic() && !playlist.getOwner().getId().equals(user.getId())) {
                        throw new RuntimeException("Access denied");
                }

                return convertToResponse(playlist, true);
        }

        /**
         * Updates an existing playlist's metadata.
         * 
         * <p>
         * Only the playlist owner can update it.
         * </p>
         * 
         * @param id       the playlist ID
         * @param request  the update request with new metadata
         * @param username the owner's username
         * @return PlaylistResponse with updated playlist details
         * @throws RuntimeException if user not found or playlist not found/access
         *                          denied
         */
        @Transactional
        public PlaylistResponse updatePlaylist(Long id, UpdatePlaylistRequest request, String username) {
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Playlist playlist = playlistRepository.findByIdAndOwner(id, user)
                                .orElseThrow(() -> new RuntimeException("Playlist not found or access denied"));

                playlist.setName(request.getName());
                playlist.setDescription(request.getDescription());
                if (request.getIsPublic() != null) {
                        playlist.setIsPublic(request.getIsPublic());
                }

                Playlist updated = playlistRepository.save(playlist);
                return convertToResponse(updated, true);
        }

        /**
         * Deletes a playlist.
         * 
         * <p>
         * Only the playlist owner can delete it.
         * </p>
         * 
         * @param id       the playlist ID
         * @param username the owner's username
         * @throws RuntimeException if user not found or playlist not found/access
         *                          denied
         */
        @Transactional
        public void deletePlaylist(Long id, String username) {
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Playlist playlist = playlistRepository.findByIdAndOwner(id, user)
                                .orElseThrow(() -> new RuntimeException("Playlist not found or access denied"));

                if (playlist != null) {
                        playlistRepository.delete(playlist);
                }
        }

        /**
         * Adds a track to a playlist.
         * 
         * <p>
         * Only the playlist owner can add tracks. If the track is already
         * in the playlist, this operation has no effect (idempotent).
         * </p>
         * 
         * @param playlistId the playlist ID
         * @param trackId    the track ID to add
         * @param username   the owner's username
         * @return PlaylistResponse with updated playlist and tracks
         * @throws RuntimeException if user, playlist, or track not found, or access
         *                          denied
         */
        @Transactional
        public PlaylistResponse addTrackToPlaylist(Long playlistId, Long trackId, String username) {
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Playlist playlist = playlistRepository.findByIdAndOwner(playlistId, user)
                                .orElseThrow(() -> new RuntimeException("Playlist not found or access denied"));

                if (trackId == null) {
                        throw new RuntimeException("Track ID cannot be null");
                }

                Track track = trackRepository.findById(trackId)
                                .orElseThrow(() -> new RuntimeException("Track not found"));

                if (!playlist.getTracks().contains(track)) {
                        playlist.getTracks().add(track);
                        playlistRepository.save(playlist);
                }

                return convertToResponse(playlist, true);
        }

        /**
         * Removes a track from a playlist.
         * 
         * <p>
         * Only the playlist owner can remove tracks.
         * </p>
         * 
         * @param playlistId the playlist ID
         * @param trackId    the track ID to remove
         * @param username   the owner's username
         * @return PlaylistResponse with updated playlist and tracks
         * @throws RuntimeException if user, playlist, or track not found, or access
         *                          denied
         */
        @Transactional
        public PlaylistResponse removeTrackFromPlaylist(Long playlistId, Long trackId, String username) {
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Playlist playlist = playlistRepository.findByIdAndOwner(playlistId, user)
                                .orElseThrow(() -> new RuntimeException("Playlist not found or access denied"));

                if (trackId == null) {
                        throw new RuntimeException("Track ID cannot be null");
                }

                Track track = trackRepository.findById(trackId)
                                .orElseThrow(() -> new RuntimeException("Track not found"));

                playlist.getTracks().remove(track);
                playlistRepository.save(playlist);

                return convertToResponse(playlist, true);
        }

        /**
         * Reorders tracks in a playlist.
         * 
         * <p>
         * Only the playlist owner can reorder tracks. The provided track IDs
         * must all exist in the playlist.
         * </p>
         * 
         * @param playlistId the playlist ID
         * @param trackIds   ordered list of track IDs in desired order
         * @param username   the owner's username
         * @return PlaylistResponse with reordered tracks
         * @throws RuntimeException if user or playlist not found, access denied, or
         *                          track not in playlist
         */
        @Transactional
        public PlaylistResponse reorderTracks(Long playlistId, List<Long> trackIds, String username) {
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Playlist playlist = playlistRepository.findByIdAndOwner(playlistId, user)
                                .orElseThrow(() -> new RuntimeException("Playlist not found or access denied"));

                List<Track> reorderedTracks = trackIds.stream()
                                .map(id -> playlist.getTracks().stream()
                                                .filter(t -> t.getId().equals(id))
                                                .findFirst()
                                                .orElseThrow(() -> new RuntimeException("Track not found in playlist")))
                                .collect(Collectors.toList());

                playlist.getTracks().clear();
                playlist.getTracks().addAll(reorderedTracks);
                playlistRepository.save(playlist);

                return convertToResponse(playlist, true);
        }

        /**
         * Searches for playlists by name or description.
         * 
         * <p>
         * Returns playlists that match the query and are either owned by
         * the user or are public.
         * </p>
         * 
         * @param query    the search query string
         * @param username the requesting user's username
         * @return list of matching playlists
         * @throws RuntimeException if user is not found
         */
        @Transactional(readOnly = true)
        public List<PlaylistResponse> searchPlaylists(String query, String username) {
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("User not found"));
                return playlistRepository.searchPlaylists(user, query).stream()
                                .map(playlist -> convertToResponse(playlist, false))
                                .collect(Collectors.toList());
        }

        /**
         * Converts a Playlist entity to a PlaylistResponse DTO.
         * 
         * @param playlist      the playlist entity
         * @param includeTracks whether to include full track details in response
         * @return PlaylistResponse DTO
         */
        private PlaylistResponse convertToResponse(Playlist playlist, boolean includeTracks) {
                PlaylistResponse response = new PlaylistResponse();
                response.setId(playlist.getId());
                response.setName(playlist.getName());
                response.setDescription(playlist.getDescription());
                response.setOwnerUsername(playlist.getOwner().getUsername());
                response.setIsPublic(playlist.getIsPublic());
                response.setCoverImageUrl(playlist.getCoverImageUrl());
                response.setTrackCount(playlist.getTrackCount());
                response.setTotalDuration(playlist.getTotalDuration());
                response.setCreatedAt(playlist.getCreatedAt());
                response.setUpdatedAt(playlist.getUpdatedAt());

                if (includeTracks) {
                        response.setTracks(playlist.getTracks().stream()
                                        .map(this::convertTrackToResponse)
                                        .collect(Collectors.toList()));
                }

                return response;
        }

        /**
         * Converts a Track entity to a TrackResponse DTO for playlist responses.
         * 
         * @param track the track entity
         * @return TrackResponse DTO
         */
        private PlaylistResponse.TrackResponse convertTrackToResponse(Track track) {
                PlaylistResponse.TrackResponse response = new PlaylistResponse.TrackResponse();
                response.setId(track.getId());
                response.setTitle(track.getTitle());
                response.setArtist(track.getArtist());
                response.setAlbum(track.getAlbum());
                response.setDuration(track.getDuration());
                response.setCoverArtUrl(track.getCoverArtUrl());
                response.setPlayCount(track.getPlayCount());
                return response;
        }
}