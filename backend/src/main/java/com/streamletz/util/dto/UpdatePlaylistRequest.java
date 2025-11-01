package com.streamletz.util.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePlaylistRequest {

    @NotBlank(message = "Playlist name is required")
    private String name;

    private String description;

    private Boolean isPublic;
}
