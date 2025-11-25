package com.streamletz.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a music playlist in the Streamletz application.
 * 
 * <p>
 * This class manages collections of tracks organized by users. Playlists can be
 * either public (visible to all users) or private (visible only to the owner).
 * The track order is maintained using JPA's {@code @OrderColumn} annotation.
 * </p>
 * 
 * <p>
 * Each playlist maintains metadata including name, description, cover image,
 * and timestamps for creation and last update.
 * </p>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "playlists")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Playlist {

    /**
     * Unique identifier for the playlist.
     * Auto-generated using database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the playlist.
     * 
     * <p>
     * Required field that serves as the playlist's title displayed to users.
     * </p>
     */
    @NotBlank
    @Column(nullable = false)
    private String name;

    /**
     * Optional description of the playlist.
     * 
     * <p>
     * Can contain up to 500 characters describing the playlist's theme,
     * mood, or purpose.
     * </p>
     */
    @Column(length = 500)
    private String description;

    /**
     * User who owns and created this playlist.
     * 
     * <p>
     * Lazily loaded relationship to the User entity. The owner has full
     * control over the playlist including modification and deletion rights.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    /**
     * Visibility flag indicating if the playlist is publicly accessible.
     * 
     * <p>
     * Defaults to {@code false} (private). When {@code true}, the playlist
     * can be discovered and viewed by other users. When {@code false}, only
     * the owner can access it.
     * </p>
     */
    @Column(nullable = false)
    private Boolean isPublic = false;

    /**
     * URL or path to the playlist's cover image.
     * 
     * <p>
     * Optional field for visual customization. If not set, a default
     * or generated image may be used by the client application.
     * </p>
     */
    @Column
    private String coverImageUrl;

    /**
     * Ordered list of tracks in this playlist.
     * 
     * <p>
     * Maintains track order using the {@code position} column in the
     * join table. Tracks can appear in multiple playlists without duplication.
     * </p>
     */
    @ManyToMany
    @JoinTable(name = "playlist_tracks", joinColumns = @JoinColumn(name = "playlist_id"), inverseJoinColumns = @JoinColumn(name = "track_id"))
    @OrderColumn(name = "position")
    private List<Track> tracks = new ArrayList<>();

    /**
     * Timestamp when the playlist was created.
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
     * Timestamp when the playlist was last modified.
     * 
     * <p>
     * Automatically updated by Hibernate whenever the entity is modified,
     * including when tracks are added or removed.
     * </p>
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Returns the number of tracks in this playlist.
     * 
     * @return the count of tracks, or 0 if the tracks list is null
     */
    public int getTrackCount() {
        return tracks != null ? tracks.size() : 0;
    }

    /**
     * Calculates the total duration of all tracks in the playlist.
     * 
     * <p>
     * Sums the duration of each track, treating null durations as 0.
     * This is useful for displaying the total playlist length to users.
     * </p>
     * 
     * @return the total duration in seconds, or 0 if the tracks list is null
     */
    public int getTotalDuration() {
        return tracks != null ? tracks.stream()
                .mapToInt(track -> track.getDuration() != null ? track.getDuration() : 0)
                .sum() : 0;
    }
}