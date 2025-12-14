package com.streamletz.util.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for paginated track browsing.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackBrowseResponse {

    private List<TrackListItemResponse> items;

    /**
     * Cursor for the next page; null when there is no next page.
     */
    private String nextCursor;

    /**
     * Whether there are more items after this page.
     */
    private boolean hasMore;
}