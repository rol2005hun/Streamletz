package com.streamletz.util.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for playlist responses.
 * 
 * <p>
 * Contains complete playlist information including metadata, statistics,
 * and optionally the list of tracks in the playlist.
 * </p>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistResponse {

    private Long id;
    private String name;
    private String description;
    private String ownerUsername;
    private Boolean isPublic;
    private String coverImageUrl;
    private Integer trackCount;
    private Integer totalDuration;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TrackResponse> tracks;

    /**
     * Nested DTO for track information within playlist responses.
     * 
     * <p>
     * Contains essential track metadata for display in playlist contexts.
     * </p>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackResponse {
        private Long id;
        private String title;
        private String artist;
        private String album;
        private Integer duration;
        private String coverArtUrl;
        private Integer playCount;
    }
}