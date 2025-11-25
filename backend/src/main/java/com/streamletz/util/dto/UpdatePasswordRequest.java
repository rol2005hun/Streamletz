package com.streamletz.util.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Data Transfer Object for password change requests.
 * 
 * <p>
 * Contains current password for verification and new password with
 * confirmation.
 * All fields are validated using Jakarta Bean Validation annotations.
 * </p>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
@Data
public class UpdatePasswordRequest {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 6, max = 100, message = "New password must be between 6 and 100 characters")
    private String newPassword;

    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;
}