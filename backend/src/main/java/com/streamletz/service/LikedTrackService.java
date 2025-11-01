package com.streamletz.service;

import com.streamletz.model.LikedTrack;
import com.streamletz.model.Track;
import com.streamletz.model.User;
import com.streamletz.repository.LikedTrackRepository;
import com.streamletz.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LikedTrackService {

    private final LikedTrackRepository likedTrackRepository;
    private final TrackRepository trackRepository;

    @Transactional
    public void likeTrack(Long trackId, User user) {
        if (trackId == null) {
            throw new IllegalArgumentException("Track ID cannot be null");
        }

        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new RuntimeException("Track not found"));

        if (!likedTrackRepository.existsByUserAndTrack(user, track)) {
            LikedTrack likedTrack = new LikedTrack();
            likedTrack.setUser(user);
            likedTrack.setTrack(track);
            likedTrackRepository.save(likedTrack);
        }
    }

    @Transactional
    public void unlikeTrack(Long trackId, User user) {
        if (trackId == null) {
            throw new IllegalArgumentException("Track ID cannot be null");
        }

        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new RuntimeException("Track not found"));

        likedTrackRepository.deleteByUserAndTrack(user, track);
    }

    @Transactional(readOnly = true)
    public List<Track> getLikedTracks(User user) {
        return likedTrackRepository.findLikedTracksByUser(user);
    }

    @Transactional(readOnly = true)
    public boolean isTrackLiked(Long trackId, User user) {
        if (trackId == null) {
            throw new IllegalArgumentException("Track ID cannot be null");
        }

        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new RuntimeException("Track not found"));

        return likedTrackRepository.existsByUserAndTrack(user, track);
    }

    @Transactional(readOnly = true)
    public long getLikedTracksCount(User user) {
        return likedTrackRepository.countByUser(user);
    }
}