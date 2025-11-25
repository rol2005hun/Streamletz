package com.streamletz.repository;

import com.streamletz.model.Playlist;
import com.streamletz.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link Playlist} entities.
 * 
 * <p>
 * Provides CRUD operations and custom query methods for playlist management,
 * including owner-based lookups, public playlist retrieval, accessibility
 * checks,
 * and full-text search capabilities.
 * </p>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    /**
     * Finds all playlists owned by a specific user.
     * 
     * @param owner the user who owns the playlists
     * @return a list of playlists owned by the specified user
     */
    List<Playlist> findByOwner(User owner);

    /**
     * Finds all playlists owned by a specific user, ordered by creation date
     * descending.
     * 
     * @param owner the user who owns the playlists
     * @return a list of playlists owned by the specified user, newest first
     */
    List<Playlist> findByOwnerOrderByCreatedAtDesc(User owner);

    /**
     * Finds all public playlists that are visible to all users.
     * 
     * @return a list of all public playlists
     */
    List<Playlist> findByIsPublicTrue();

    /**
     * Finds all playlists that are accessible to a specific user.
     * 
     * <p>
     * Returns playlists that are either owned by the user or marked as public.
     * This allows users to see both their own playlists and public playlists
     * created by others.
     * </p>
     * 
     * @param owner the user for whom to find accessible playlists
     * @return a list of playlists accessible to the specified user
     */
    @Query("SELECT p FROM Playlist p WHERE p.owner = :owner OR p.isPublic = true")
    List<Playlist> findAccessiblePlaylists(@Param("owner") User owner);

    /**
     * Searches for playlists accessible to a user with names or descriptions
     * matching the search query.
     * 
     * <p>
     * The search is case-insensitive and matches partial strings in both
     * the playlist name and description fields. Only returns playlists that
     * the user owns or that are public.
     * </p>
     * 
     * @param user  the user performing the search
     * @param query the search string to match against playlist names and
     *              descriptions
     * @return a list of matching playlists accessible to the user
     */
    @Query("SELECT p FROM Playlist p WHERE (p.owner = :user OR p.isPublic = true) " +
            "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Playlist> searchPlaylists(@Param("user") User user, @Param("query") String query);

    /**
     * Finds a playlist by its ID and owner.
     * 
     * <p>
     * Useful for verifying ownership before allowing modification or deletion.
     * </p>
     * 
     * @param id    the playlist ID
     * @param owner the expected owner of the playlist
     * @return an Optional containing the playlist if found and owned by the user,
     *         empty otherwise
     */
    Optional<Playlist> findByIdAndOwner(Long id, User owner);
}
