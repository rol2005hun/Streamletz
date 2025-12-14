package com.streamletz.util.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO used for batching track id based operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackIdsRequest {
    private List<Long> trackIds;
}