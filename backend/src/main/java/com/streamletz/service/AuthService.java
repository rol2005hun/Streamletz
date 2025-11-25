package com.streamletz.service;

import com.streamletz.config.JwtTokenProvider;
import com.streamletz.model.User;
import com.streamletz.repository.UserRepository;
import com.streamletz.util.dto.LoginRequest;
import com.streamletz.util.dto.RegisterRequest;
import com.streamletz.util.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Service class for handling user authentication and registration.
 * 
 * <p>
 * This service manages user account creation and login processes,
 * including password encryption, JWT token generation, and validation
 * of credentials using Spring Security's authentication framework.
 * </p>
 * 
 * <p>
 * All new users are automatically assigned the "ROLE_USER" role
 * upon registration.
 * </p>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Registers a new user in the system.
     * 
     * <p>
     * Creates a new user account with the provided credentials, assigns
     * default user role, and generates a JWT token for immediate authentication.
     * Passwords are encrypted using BCrypt before storage.
     * </p>
     * 
     * @param request the registration request containing username, email, and
     *                password
     * @return an AuthResponse containing the JWT token and user information
     * @throws RuntimeException if the username or email already exists
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        user.setRoles(roles);

        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtTokenProvider.generateToken(userDetails);

        return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getProfileImage());
    }

    /**
     * Authenticates a user and generates a JWT token.
     * 
     * <p>
     * Validates the user's credentials using Spring Security's
     * AuthenticationManager
     * and generates a JWT token for accessing protected resources.
     * </p>
     * 
     * @param request the login request containing username and password
     * @return an AuthResponse containing the JWT token and user information
     * @throws RuntimeException if authentication fails or user is not found
     */
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtTokenProvider.generateToken(userDetails);

        return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getProfileImage());
    }
}