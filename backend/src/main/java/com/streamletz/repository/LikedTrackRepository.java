package com.streamletz.repository;

import com.streamletz.model.LikedTrack;
import com.streamletz.model.Track;
import com.streamletz.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link LikedTrack} entities.
 * 
 * <p>
 * Provides CRUD operations and custom query methods for managing user-track
 * like relationships, including existence checks, deletion, and retrieval of
 * liked tracks ordered by like timestamp.
 * </p>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface LikedTrackRepository extends JpaRepository<LikedTrack, Long> {

    /**
     * Finds the like relationship between a specific user and track.
     * 
     * @param user  the user who liked the track
     * @param track the track that was liked
     * @return an Optional containing the LikedTrack entity if found, empty
     *         otherwise
     */
    Optional<LikedTrack> findByUserAndTrack(User user, Track track);

    /**
     * Finds all tracks liked by a user, ordered by most recently liked first.
     * 
     * @param user the user whose liked tracks to retrieve
     * @return a list of LikedTrack entities ordered by likedAt descending
     */
    List<LikedTrack> findByUserOrderByLikedAtDesc(User user);

    /**
     * Checks if a user has liked a specific track.
     * 
     * @param user  the user to check
     * @param track the track to check
     * @return true if the user has liked the track, false otherwise
     */
    boolean existsByUserAndTrack(User user, Track track);

    /**
     * Deletes the like relationship between a user and track.
     * 
     * <p>
     * This method should be called within a transaction to ensure proper cleanup.
     * </p>
     * 
     * @param user  the user who liked the track
     * @param track the track to unlike
     */
    void deleteByUserAndTrack(User user, Track track);

    /**
     * Retrieves only the Track entities that a user has liked, ordered by like
     * date.
     * 
     * <p>
     * This query projection is more efficient than fetching full LikedTrack
     * entities when only track information is needed.
     * </p>
     * 
     * @param user the user whose liked tracks to retrieve
     * @return a list of Track entities ordered by when they were liked (most recent
     *         first)
     */
    @Query("SELECT lt.track FROM LikedTrack lt WHERE lt.user = :user ORDER BY lt.likedAt DESC")
    List<Track> findLikedTracksByUser(@Param("user") User user);

    @Query("SELECT lt.track.id FROM LikedTrack lt WHERE lt.user = :user AND lt.track.id IN :trackIds")
    List<Long> findLikedTrackIdsByUserAndTrackIds(@Param("user") User user,
                                                  @Param("trackIds") List<Long> trackIds);

    /**
     * Counts the total number of tracks liked by a user.
     * 
     * @param user the user whose liked tracks to count
     * @return the number of tracks liked by the user
     */
    long countByUser(User user);
}
