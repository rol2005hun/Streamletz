package com.streamletz.util.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String profileImage;
    private LocalDateTime createdAt;
    private String newToken;
}