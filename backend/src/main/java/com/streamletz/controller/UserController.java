package com.streamletz.controller;

import com.streamletz.service.UserService;
import com.streamletz.util.dto.UpdatePasswordRequest;
import com.streamletz.util.dto.UpdateProfileRequest;
import com.streamletz.util.dto.UpdateProfileResponse;
import com.streamletz.util.dto.UserProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing user profiles and account settings.
 * 
 * <p>
 * This controller provides endpoints for viewing and updating user profile
 * information, including username, email, profile image, and password changes.
 * </p>
 * 
 * <p>
 * All endpoints require JWT authentication except for public profile viewing.
 * </p>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "User profile management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    /**
     * Retrieves the authenticated user's profile information.
     * 
     * <p>
     * Returns the current user's profile details including username, email,
     * profile image, and account creation date.
     * </p>
     * 
     * @param authentication the authentication object containing the current user's
     *                       details
     * @return ResponseEntity containing the user's profile information
     */
    @GetMapping("/profile")
    @Operation(summary = "Get user profile", description = "Get current user's profile information")
    public ResponseEntity<UserProfileResponse> getUserProfile(Authentication authentication) {
        String username = authentication.getName();
        UserProfileResponse profile = userService.getUserProfile(username);
        return ResponseEntity.ok(profile);
    }

    /**
     * Retrieves a user's public profile by ID or username.
     * 
     * <p>
     * This endpoint is publicly accessible and returns profile information
     * for any user. The identifier can be either a numeric user ID or a username.
     * </p>
     * 
     * @param identifier the user ID (numeric) or username to look up
     * @return ResponseEntity containing the user's public profile information
     * @throws RuntimeException if the user is not found
     */
    @GetMapping("/profile/{identifier}")
    @Operation(summary = "Get public profile", description = "Get any user's public profile by ID or username")
    public ResponseEntity<UserProfileResponse> getPublicProfile(@PathVariable String identifier) {
        UserProfileResponse profile = userService.getPublicProfile(identifier);
        return ResponseEntity.ok(profile);
    }

    /**
     * Updates the authenticated user's profile information.
     * 
     * <p>
     * Allows updating username, email, and profile image. If the username is
     * changed,
     * a new JWT token is generated and returned in the response. The request body
     * is
     * validated for proper format and uniqueness constraints.
     * </p>
     * 
     * @param request        the update request containing new profile information
     * @param authentication the authentication object containing the current user's
     *                       details
     * @return ResponseEntity containing the updated profile and optionally a new
     *         JWT token
     * @throws RuntimeException if the new username or email is already taken
     */
    @PutMapping("/profile")
    @Operation(summary = "Update user profile", description = "Update user's profile information")
    public ResponseEntity<UpdateProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        UpdateProfileResponse profile = userService.updateProfile(username, request);
        return ResponseEntity.ok(profile);
    }

    /**
     * Changes the authenticated user's password.
     * 
     * <p>
     * Requires the current password for verification and validates that the new
     * password matches the confirmation. The request body is validated for proper
     * format.
     * </p>
     * 
     * @param request        the password change request containing current and new
     *                       passwords
     * @param authentication the authentication object containing the current user's
     *                       details
     * @return ResponseEntity with no content on successful password change
     * @throws RuntimeException if the current password is incorrect or passwords
     *                          don't match
     */
    @PutMapping("/password")
    @Operation(summary = "Change password", description = "Change user's password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody UpdatePasswordRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        userService.changePassword(username, request);
        return ResponseEntity.ok().build();
    }
}