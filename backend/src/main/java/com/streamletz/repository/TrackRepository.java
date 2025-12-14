package com.streamletz.repository;

import com.streamletz.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link Track} entities.
 * 
 * <p>
 * Provides CRUD operations and custom query methods for track management,
 * including case-insensitive search by title, artist, and album, as well as
 * lookups by uploader and file path.
 * </p>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface TrackRepository extends JpaRepository<Track, Long> {

    /**
     * Finds tracks with titles containing the specified string (case-insensitive).
     * 
     * @param title the search string to match against track titles
     * @return a list of tracks matching the search criteria
     */
    List<Track> findByTitleContainingIgnoreCase(String title);

    /**
     * Finds tracks with artists containing the specified string (case-insensitive).
     * 
     * @param artist the search string to match against artist names
     * @return a list of tracks matching the search criteria
     */
    List<Track> findByArtistContainingIgnoreCase(String artist);

    /**
     * Finds tracks with album names containing the specified string
     * (case-insensitive).
     * 
     * @param album the search string to match against album names
     * @return a list of tracks matching the search criteria
     */
    List<Track> findByAlbumContainingIgnoreCase(String album);

    /**
     * Finds all tracks uploaded by a specific user.
     * 
     * @param userId the ID of the user who uploaded the tracks
     * @return a list of tracks uploaded by the specified user
     */
    List<Track> findByUploadedById(Long userId);

    /**
     * Finds a track by its file system path.
     * 
     * @param filePath the file system path of the track
     * @return an Optional containing the track if found, empty otherwise
     */
    Optional<Track> findByFilePath(String filePath);

    @Query("SELECT t FROM Track t ORDER BY t.createdAt DESC, t.id DESC")
    List<Track> browseFirstPage(Pageable pageable);

    @Query("SELECT t FROM Track t WHERE (t.createdAt < :createdAt) OR (t.createdAt = :createdAt AND t.id < :id) ORDER BY t.createdAt DESC, t.id DESC")
    List<Track> browseAfterCursor(@Param("createdAt") LocalDateTime createdAt,
                                  @Param("id") Long id,
                                  Pageable pageable);
}