package com.streamletz.service;

import com.streamletz.config.JwtTokenProvider;
import com.streamletz.model.User;
import com.streamletz.repository.UserRepository;
import com.streamletz.util.dto.UpdatePasswordRequest;
import com.streamletz.util.dto.UpdateProfileRequest;
import com.streamletz.util.dto.UpdateProfileResponse;
import com.streamletz.util.dto.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing user profiles and account settings.
 * 
 * <p>
 * This service handles user profile operations including:
 * </p>
 * <ul>
 * <li>Retrieving user profile information</li>
 * <li>Updating profile details (username, email, profile image)</li>
 * <li>Changing user passwords with validation</li>
 * <li>Generating new JWT tokens when username changes</li>
 * </ul>
 * 
 * <p>
 * All operations are transactional to ensure data consistency.
 * </p>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Retrieves the profile information for the authenticated user.
     * 
     * @param username the username of the user
     * @return UserProfileResponse containing user details
     * @throws RuntimeException if the user is not found
     */
    public UserProfileResponse getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Retrieves a user's public profile by ID or username.
     * 
     * <p>
     * Attempts to parse the identifier as a Long ID first. If that fails,
     * treats it as a username string.
     * </p>
     * 
     * @param identifier the user ID (numeric) or username to look up
     * @return UserProfileResponse containing user details
     * @throws RuntimeException if the user is not found
     */
    public UserProfileResponse getPublicProfile(String identifier) {
        User user;

        try {
            Long id = Long.parseLong(identifier);
            user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        } catch (NumberFormatException e) {
            user = userRepository.findByUsername(identifier)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Updates the user's profile information.
     * 
     * <p>
     * Allows updating username, email, and profile image. If the username is
     * changed,
     * a new JWT token is generated and included in the response. Validates that new
     * username and email are not already taken by other users.
     * </p>
     * 
     * @param username the current username of the user
     * @param request  the update request containing new profile information
     * @return UpdateProfileResponse with updated profile and optionally a new JWT
     *         token
     * @throws RuntimeException if user not found, username taken, or email taken
     */
    public UpdateProfileResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean usernameChanged = false;

        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("Username is already taken");
            }
            user.setUsername(request.getUsername());
            usernameChanged = true;
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email is already taken");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getProfileImage() != null) {
            user.setProfileImage(request.getProfileImage());
        }

        @SuppressWarnings("null")
        User savedUser = userRepository.save(user);

        String newToken = null;
        if (usernameChanged) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getUsername());
            newToken = jwtTokenProvider.generateToken(userDetails);
        }

        return UpdateProfileResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .profileImage(savedUser.getProfileImage())
                .createdAt(savedUser.getCreatedAt())
                .newToken(newToken)
                .build();
    }

    /**
     * Changes the user's password.
     * 
     * <p>
     * Validates the current password, ensures new password matches confirmation,
     * then encrypts and saves the new password.
     * </p>
     * 
     * @param username the username of the user
     * @param request  the password change request with current and new passwords
     * @throws RuntimeException if user not found, current password incorrect, or
     *                          passwords don't match
     */
    public void changePassword(String username, UpdatePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password and confirmation do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}