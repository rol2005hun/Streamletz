package com.streamletz.service;

import com.streamletz.model.LikedTrack;
import com.streamletz.model.Track;
import com.streamletz.model.User;
import com.streamletz.repository.LikedTrackRepository;
import com.streamletz.repository.TrackRepository;
import com.streamletz.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class for managing user's liked tracks.
 * 
 * <p>
 * This service handles all operations related to users liking and unliking
 * tracks,
 * including:
 * </p>
 * <ul>
 * <li>Adding tracks to a user's liked collection</li>
 * <li>Removing tracks from a user's liked collection</li>
 * <li>Retrieving all liked tracks for a user</li>
 * <li>Checking if a specific track is liked by a user</li>
 * <li>Getting the total count of liked tracks</li>
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
public class LikedTrackService {

    private final LikedTrackRepository likedTrackRepository;
    private final TrackRepository trackRepository;
    private final UserRepository userRepository;

    /**
     * Adds a track to the user's liked tracks collection.
     * 
     * <p>
     * If the track is already liked by the user, this operation has no effect
     * (idempotent operation). Creates a new {@link LikedTrack} entry with the
     * current timestamp.
     * </p>
     * 
     * @param trackId  the ID of the track to like
     * @param username the username of the user liking the track
     * @throws IllegalArgumentException if trackId is null
     * @throws RuntimeException         if the user or track is not found
     */
    @Transactional
    public void likeTrack(Long trackId, String username) {
        if (trackId == null) {
            throw new IllegalArgumentException("Track ID cannot be null");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new RuntimeException("Track not found"));

        if (!likedTrackRepository.existsByUserAndTrack(user, track)) {
            LikedTrack likedTrack = new LikedTrack();
            likedTrack.setUser(user);
            likedTrack.setTrack(track);
            likedTrackRepository.save(likedTrack);
        }
    }

    /**
     * Removes a track from the user's liked tracks collection.
     * 
     * <p>
     * If the track is not in the user's liked collection, this operation
     * has no effect (idempotent operation).
     * </p>
     * 
     * @param trackId  the ID of the track to unlike
     * @param username the username of the user unliking the track
     * @throws IllegalArgumentException if trackId is null
     * @throws RuntimeException         if the user or track is not found
     */
    @Transactional
    public void unlikeTrack(Long trackId, String username) {
        if (trackId == null) {
            throw new IllegalArgumentException("Track ID cannot be null");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new RuntimeException("Track not found"));

        likedTrackRepository.deleteByUserAndTrack(user, track);
    }

    /**
     * Retrieves all tracks liked by the specified user.
     * 
     * <p>
     * Returns tracks ordered by when they were liked, most recent first.
     * </p>
     * 
     * @param username the username whose liked tracks to retrieve
     * @return list of tracks liked by the user
     * @throws RuntimeException if the user is not found
     */
    @Transactional(readOnly = true)
    public List<Track> getLikedTracks(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return likedTrackRepository.findLikedTracksByUser(user);
    }

    /**
     * Checks if a specific track is liked by the user.
     * 
     * @param trackId  the ID of the track to check
     * @param username the username to check for
     * @return true if the track is liked by the user, false otherwise
     * @throws IllegalArgumentException if trackId is null
     * @throws RuntimeException         if the user or track is not found
     */
    @Transactional(readOnly = true)
    public boolean isTrackLiked(Long trackId, String username) {
        if (trackId == null) {
            throw new IllegalArgumentException("Track ID cannot be null");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new RuntimeException("Track not found"));

        return likedTrackRepository.existsByUserAndTrack(user, track);
    }

    /**
     * Gets the total count of tracks liked by the user.
     * 
     * @param username the username whose liked track count to retrieve
     * @return the number of tracks liked by the user
     * @throws RuntimeException if the user is not found
     */
    @Transactional(readOnly = true)
    public long getLikedTracksCount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return likedTrackRepository.countByUser(user);
    }
}