package com.streamletz.util.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for playlist creation requests.
 * 
 * <p>
 * Contains required and optional information for creating a new playlist.
 * Playlists are private by default unless explicitly set to public.
 * </p>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePlaylistRequest {

    @NotBlank(message = "Playlist name is required")
    private String name;

    private String description;

    private Boolean isPublic = false;
}