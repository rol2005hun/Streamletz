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

@Repository
public interface LikedTrackRepository extends JpaRepository<LikedTrack, Long> {

    Optional<LikedTrack> findByUserAndTrack(User user, Track track);

    List<LikedTrack> findByUserOrderByLikedAtDesc(User user);

    boolean existsByUserAndTrack(User user, Track track);

    void deleteByUserAndTrack(User user, Track track);

    @Query("SELECT lt.track FROM LikedTrack lt WHERE lt.user = :user ORDER BY lt.likedAt DESC")
    List<Track> findLikedTracksByUser(@Param("user") User user);

    long countByUser(User user);
}
