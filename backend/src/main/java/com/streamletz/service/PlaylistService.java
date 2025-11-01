package com.streamletz.service;

import com.streamletz.model.Playlist;
import com.streamletz.model.Track;
import com.streamletz.model.User;
import com.streamletz.repository.PlaylistRepository;
import com.streamletz.repository.TrackRepository;
import com.streamletz.util.dto.CreatePlaylistRequest;
import com.streamletz.util.dto.PlaylistResponse;
import com.streamletz.util.dto.UpdatePlaylistRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final TrackRepository trackRepository;

    @Transactional
    public PlaylistResponse createPlaylist(CreatePlaylistRequest request, User user) {
        Playlist playlist = new Playlist();
        playlist.setName(request.getName());
        playlist.setDescription(request.getDescription());
        playlist.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : false);
        playlist.setOwner(user);

        Playlist saved = playlistRepository.save(playlist);
        return convertToResponse(saved, false);
    }

    @Transactional(readOnly = true)
    public List<PlaylistResponse> getUserPlaylists(User user) {
        return playlistRepository.findByOwnerOrderByCreatedAtDesc(user).stream()
                .map(playlist -> convertToResponse(playlist, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PlaylistResponse> getPublicPlaylists() {
        return playlistRepository.findByIsPublicTrue().stream()
                .map(playlist -> convertToResponse(playlist, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlaylistResponse getPlaylistById(Long id, User user) {
        if (id == null) {
            throw new RuntimeException("Playlist ID cannot be null");
        }

        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));

        if (!playlist.getIsPublic() && !playlist.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        return convertToResponse(playlist, true);
    }

    @Transactional
    public PlaylistResponse updatePlaylist(Long id, UpdatePlaylistRequest request, User user) {
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

    @Transactional
    public void deletePlaylist(Long id, User user) {
        Playlist playlist = playlistRepository.findByIdAndOwner(id, user)
                .orElseThrow(() -> new RuntimeException("Playlist not found or access denied"));

        if (playlist != null) {
            playlistRepository.delete(playlist);
        }
    }

    @Transactional
    public PlaylistResponse addTrackToPlaylist(Long playlistId, Long trackId, User user) {
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

    @Transactional
    public PlaylistResponse removeTrackFromPlaylist(Long playlistId, Long trackId, User user) {
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

    @Transactional
    public PlaylistResponse reorderTracks(Long playlistId, List<Long> trackIds, User user) {
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

    @Transactional(readOnly = true)
    public List<PlaylistResponse> searchPlaylists(String query, User user) {
        return playlistRepository.searchPlaylists(user, query).stream()
                .map(playlist -> convertToResponse(playlist, false))
                .collect(Collectors.toList());
    }

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