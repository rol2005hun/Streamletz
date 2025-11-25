package com.streamletz.controller;

import com.streamletz.service.AuthService;
import com.streamletz.util.dto.AuthResponse;
import com.streamletz.util.dto.LoginRequest;
import com.streamletz.util.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for handling user authentication operations.
 * 
 * <p>
 * This controller provides endpoints for user registration and login,
 * returning JWT tokens for authenticated access to protected resources.
 * </p>
 * 
 * <p>
 * All endpoints in this controller are publicly accessible and do not
 * require authentication.
 * </p>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication endpoints")
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user account.
     * 
     * <p>
     * Creates a new user with the provided credentials and returns a JWT token
     * for immediate authentication. The request body is validated for proper format
     * and required fields.
     * </p>
     * 
     * @param request the registration request containing username, email, and
     *                password
     * @return ResponseEntity containing the authentication response with JWT token
     * @throws RuntimeException if username or email already exists
     */
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new user account")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Authenticates a user and returns a JWT token.
     * 
     * <p>
     * Validates the user's credentials and generates a JWT token for accessing
     * protected resources. The request body is validated for proper format.
     * </p>
     * 
     * @param request the login request containing username and password
     * @return ResponseEntity containing the authentication response with JWT token
     * @throws RuntimeException if authentication fails or user is not found
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}