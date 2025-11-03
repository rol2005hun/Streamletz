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

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "User profile management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "Get user profile", description = "Get current user's profile information")
    public ResponseEntity<UserProfileResponse> getUserProfile(Authentication authentication) {
        String username = authentication.getName();
        UserProfileResponse profile = userService.getUserProfile(username);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/profile/{identifier}")
    @Operation(summary = "Get public profile", description = "Get any user's public profile by ID or username")
    public ResponseEntity<UserProfileResponse> getPublicProfile(@PathVariable String identifier) {
        UserProfileResponse profile = userService.getPublicProfile(identifier);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    @Operation(summary = "Update user profile", description = "Update user's profile information")
    public ResponseEntity<UpdateProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        UpdateProfileResponse profile = userService.updateProfile(username, request);
        return ResponseEntity.ok(profile);
    }

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