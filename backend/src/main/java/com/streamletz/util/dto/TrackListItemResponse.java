package com.streamletz.util.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Lightweight DTO for track list views (dashboard, browse, search results).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackListItemResponse {

    private Long id;
    private String title;
    private String artist;
    private String album;
    private Integer duration;
    private String coverArtUrl;
    private Integer playCount;

    /**
     * Used for cursor pagination. Not required for rendering.
     */
    private LocalDateTime createdAt;
}