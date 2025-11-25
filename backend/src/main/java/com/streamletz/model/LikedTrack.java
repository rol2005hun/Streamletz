package com.streamletz.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a user's "like" relationship with a track.
 * 
 * <p>
 * This class serves as a junction table that maintains the many-to-many
 * relationship between users and their liked tracks. It includes a timestamp
 * to track when the track was liked, enabling chronological ordering of
 * a user's liked tracks.
 * </p>
 * 
 * <p>
 * The unique constraint ensures that a user cannot like the same track
 * multiple times.
 * </p>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "liked_tracks", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "track_id" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikedTrack {

    /**
     * Unique identifier for this like relationship.
     * Auto-generated using database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who liked the track.
     * 
     * <p>
     * Lazily loaded relationship to the User entity. Required field
     * that forms part of the unique constraint.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The track that was liked by the user.
     * 
     * <p>
     * Lazily loaded relationship to the Track entity. Required field
     * that forms part of the unique constraint.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;

    /**
     * Timestamp when the user liked this track.
     * 
     * <p>
     * Automatically set by Hibernate when the entity is first persisted.
     * This field is immutable and enables chronological sorting of liked tracks.
     * </p>
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime likedAt;
}