package com.streamletz.util.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for playlist update requests.
 * 
 * <p>
 * Contains fields for updating playlist metadata including name,
 * description, and visibility settings.
 * </p>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePlaylistRequest {

    @NotBlank(message = "Playlist name is required")
    private String name;

    private String description;

    private Boolean isPublic;
}