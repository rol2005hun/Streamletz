package com.streamletz.config;

import com.streamletz.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter for processing and validating JWT tokens in HTTP
 * requests.
 * 
 * <p>
 * This filter extends Spring's {@link OncePerRequestFilter} to ensure it
 * executes
 * once per request. It intercepts incoming HTTP requests, extracts JWT tokens
 * from
 * the Authorization header, validates them, and sets up the Spring Security
 * context
 * with the authenticated user's details.
 * </p>
 * 
 * <p>
 * The filter expects JWT tokens in the format:
 * {@code Authorization: Bearer <token>}
 * </p>
 * 
 * <p>
 * If a valid token is found, the user's authentication is set in the
 * {@link SecurityContextHolder}, allowing access to protected resources.
 * </p>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Filters incoming HTTP requests to extract and validate JWT tokens.
     * 
     * <p>
     * This method is called once per request and performs the following steps:
     * </p>
     * <ol>
     * <li>Extracts the JWT token from the Authorization header</li>
     * <li>Validates the token and extracts the username</li>
     * <li>Loads user details from the database</li>
     * <li>Creates an authentication token and sets it in the security context</li>
     * <li>Passes the request to the next filter in the chain</li>
     * </ol>
     * 
     * <p>
     * If token validation fails or an exception occurs, the error is logged
     * and the request continues without authentication.
     * </p>
     * 
     * @param request     the HTTP request being processed
     * @param response    the HTTP response
     * @param filterChain the filter chain to pass the request through
     * @throws ServletException if a servlet-related error occurs
     * @throws IOException      if an I/O error occurs during filtering
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.extractUsername(jwt) != null) {
                String username = tokenProvider.extractUsername(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (tokenProvider.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT token from the Authorization header of the HTTP request.
     * 
     * <p>
     * This method looks for the "Authorization" header and extracts the token
     * if it follows the Bearer authentication scheme format:
     * {@code Bearer <token>}.
     * </p>
     * 
     * <p>
     * The "Bearer " prefix (7 characters) is removed from the header value
     * to return only the actual JWT token string.
     * </p>
     * 
     * @param request the HTTP request containing the Authorization header
     * @return the JWT token string without the "Bearer " prefix, or null if not
     *         found
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}