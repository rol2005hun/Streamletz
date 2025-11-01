package com.streamletz.repository;

import com.streamletz.model.Playlist;
import com.streamletz.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    List<Playlist> findByOwner(User owner);

    List<Playlist> findByOwnerOrderByCreatedAtDesc(User owner);

    List<Playlist> findByIsPublicTrue();

    @Query("SELECT p FROM Playlist p WHERE p.owner = :owner OR p.isPublic = true")
    List<Playlist> findAccessiblePlaylists(@Param("owner") User owner);

    @Query("SELECT p FROM Playlist p WHERE (p.owner = :user OR p.isPublic = true) " +
           "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Playlist> searchPlaylists(@Param("user") User user, @Param("query") String query);

    Optional<Playlist> findByIdAndOwner(Long id, User owner);
}
