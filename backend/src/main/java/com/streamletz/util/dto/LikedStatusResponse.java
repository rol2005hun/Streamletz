package com.streamletz.util.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for batched liked status checks.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikedStatusResponse {
    private List<Long> likedTrackIds;
}