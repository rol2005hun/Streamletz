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

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

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