package com.streamletz.util.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for profile update responses.
 * 
 * <p>
 * Contains updated user profile information and optionally a new JWT token
 * if the username was changed.
 * </p>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
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