package com.streamletz.util.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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
