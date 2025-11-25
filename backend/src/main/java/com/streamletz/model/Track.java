package com.streamletz.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a music track in the Streamletz application.
 * 
 * <p>
 * This class stores metadata and file information for audio tracks, including
 * title, artist, album, duration, and file storage details. It supports user
 * tracking
 * for uploaded content and maintains play count statistics.
 * </p>
 * 
 * <p>
 * Tracks can be associated with playlists and can be liked by users. The file
 * path
 * is required and must point to a valid audio file on the server's storage
 * system.
 * </p>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "tracks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Track {

    /**
     * Unique identifier for the track.
     * Auto-generated using database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Title of the music track.
     * 
     * <p>
     * Required field that displays the track name to users.
     * </p>
     */
    @NotBlank
    @Column(nullable = false)
    private String title;

    /**
     * Name of the artist or band that performed the track.
     * 
     * <p>
     * Required field used for filtering and displaying track credits.
     * </p>
     */
    @NotBlank
    @Column(nullable = false)
    private String artist;

    /**
     * Name of the album containing this track.
     * 
     * <p>
     * Optional field for organizing tracks by album.
     * </p>
     */
    @Column
    private String album;

    /**
     * Duration of the track in seconds.
     * 
     * <p>
     * Optional field extracted from audio file metadata. Used for displaying
     * track length and calculating playlist duration.
     * </p>
     */
    @Column
    private Integer duration;

    /**
     * URL or path to the track's cover art image.
     * 
     * <p>
     * Optional field that can reference either album art or artist image.
     * Used for visual display in the player interface.
     * </p>
     */
    @Column
    private String coverArtUrl;

    /**
     * File system path to the audio file.
     * 
     * <p>
     * Required field pointing to the physical location of the audio file
     * on the server. This path is used by the streaming service to serve
     * the audio content.
     * </p>
     */
    @NotBlank
    @Column(nullable = false)
    private String filePath;

    /**
     * Audio file format (e.g., "mp3", "flac", "wav").
     * 
     * <p>
     * Optional field extracted from file metadata. Used for client-side
     * compatibility checks and display purposes.
     * </p>
     */
    @Column
    private String fileFormat;

    /**
     * Size of the audio file in bytes.
     * 
     * <p>
     * Optional field used for storage management and bandwidth calculations.
     * </p>
     */
    @Column
    private Long fileSize;

    /**
     * User who uploaded this track to the system.
     * 
     * <p>
     * Lazily loaded relationship to the User entity. Can be null for
     * system-imported tracks.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    /**
     * Timestamp when the track was added to the database.
     * 
     * <p>
     * Automatically set by Hibernate when the entity is first persisted.
     * This field is immutable after creation.
     * </p>
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Number of times this track has been played.
     * 
     * <p>
     * Defaults to 0 and increments each time the track is streamed.
     * Used for popularity metrics and recommendations.
     * </p>
     */
    @Column
    private Integer playCount = 0;
}