package com.streamletz.config;

import com.streamletz.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security configuration for the Streamletz application.
 * 
 * <p>
 * This configuration class sets up the security infrastructure including:
 * </p>
 * <ul>
 * <li>JWT-based stateless authentication</li>
 * <li>CORS (Cross-Origin Resource Sharing) configuration</li>
 * <li>HTTP security filter chain with public and protected endpoints</li>
 * <li>BCrypt password encoding</li>
 * <li>Custom authentication provider with user details service</li>
 * </ul>
 * 
 * <p>
 * The security is configured to be stateless (no sessions) and uses JWT tokens
 * for authentication. CSRF protection is disabled as it's not needed for
 * stateless APIs.
 * </p>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtAuthFilter;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * Configures the HTTP security filter chain.
     * 
     * <p>
     * Sets up the following security rules:
     * </p>
     * <ul>
     * <li><b>CSRF:</b> Disabled (not needed for stateless JWT authentication)</li>
     * <li><b>CORS:</b> Enabled with custom configuration</li>
     * <li><b>Public endpoints:</b> Authentication, API docs, track streaming,
     * covers, public profiles</li>
     * <li><b>Protected endpoints:</b> All other endpoints require
     * authentication</li>
     * <li><b>Session management:</b> Stateless (no server-side sessions)</li>
     * <li><b>JWT filter:</b> Added before UsernamePasswordAuthenticationFilter</li>
     * </ul>
     * 
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/tracks/stream/**").permitAll()
                        .requestMatchers("/api/covers/**").permitAll()
                        .requestMatchers("/api/user/profile/*").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) settings.
     * 
     * <p>
     * Allows cross-origin requests from configured origins with:
     * </p>
     * <ul>
     * <li><b>Allowed origins:</b> Configured via {@code cors.allowed-origins}
     * property</li>
     * <li><b>Allowed methods:</b> GET, POST, PUT, DELETE, OPTIONS</li>
     * <li><b>Allowed headers:</b> All headers (*)</li>
     * <li><b>Credentials:</b> Enabled (allows cookies and authorization
     * headers)</li>
     * <li><b>Exposed headers:</b> Content-Range, Accept-Ranges, Content-Length (for
     * streaming)</li>
     * </ul>
     * 
     * @return the configured CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Content-Range", "Accept-Ranges", "Content-Length"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Creates the authentication provider for user authentication.
     * 
     * <p>
     * Configures a {@link DaoAuthenticationProvider} that:
     * </p>
     * <ul>
     * <li>Uses the custom {@link UserDetailsServiceImpl} to load user details</li>
     * <li>Uses BCrypt password encoder for password verification</li>
     * </ul>
     * 
     * @return the configured AuthenticationProvider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Provides the authentication manager from Spring Security configuration.
     * 
     * <p>
     * The authentication manager is used to authenticate users during login.
     * It delegates to the configured authentication provider.
     * </p>
     * 
     * @param config the authentication configuration
     * @return the AuthenticationManager
     * @throws Exception if an error occurs retrieving the authentication manager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Provides the password encoder for hashing and verifying passwords.
     * 
     * <p>
     * Uses {@link BCryptPasswordEncoder} which is a strong, adaptive hashing
     * algorithm designed for password storage. BCrypt automatically handles
     * salting and is resistant to brute-force attacks.
     * </p>
     * 
     * @return the BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}