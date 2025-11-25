package com.streamletz.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for generating, parsing, and validating JWT (JSON Web Token)
 * tokens.
 * 
 * <p>
 * This component handles all JWT-related operations including:
 * </p>
 * <ul>
 * <li>Token generation with configurable expiration time</li>
 * <li>Token validation and verification</li>
 * <li>Extraction of claims (username, expiration date, etc.)</li>
 * <li>HMAC-SHA signing using a secret key</li>
 * </ul>
 * 
 * <p>
 * The secret key and expiration time are configured via application properties:
 * {@code jwt.secret} and {@code jwt.expiration}.
 * </p>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Creates and returns the secret key used for signing JWT tokens.
     * 
     * <p>
     * Converts the configured secret string into a {@link SecretKey} instance
     * using HMAC-SHA algorithm. The secret is encoded using UTF-8 charset.
     * </p>
     * 
     * @return the secret key for JWT signing and verification
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Extracts the username (subject) from a JWT token.
     * 
     * @param token the JWT token string
     * @return the username stored in the token's subject claim
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from a JWT token.
     * 
     * @param token the JWT token string
     * @return the expiration date of the token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from a JWT token using a custom resolver function.
     * 
     * <p>
     * This generic method allows extraction of any claim from the token by
     * providing a function that takes {@link Claims} and returns the desired value.
     * </p>
     * 
     * @param <T>            the type of the claim value to extract
     * @param token          the JWT token string
     * @param claimsResolver a function to extract the specific claim from Claims
     * @return the extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses and extracts all claims from a JWT token.
     * 
     * <p>
     * Uses the JJWT library to parse the token, verify its signature using
     * the signing key, and extract the payload containing all claims.
     * </p>
     * 
     * @param token the JWT token string to parse
     * @return the Claims object containing all token claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Checks if a JWT token has expired.
     * 
     * @param token the JWT token string to check
     * @return true if the token's expiration date is before the current date, false
     *         otherwise
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Generates a new JWT token for the given user.
     * 
     * <p>
     * Creates a token with the username as the subject claim and sets
     * the expiration time based on the configured expiration value.
     * </p>
     * 
     * @param userDetails the user details containing the username
     * @return a signed JWT token string
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Creates a JWT token with the specified claims and subject.
     * 
     * <p>
     * Builds a JWT token with:
     * </p>
     * <ul>
     * <li>Custom claims (if any)</li>
     * <li>Subject (username)</li>
     * <li>Issued-at timestamp (current time)</li>
     * <li>Expiration timestamp (current time + configured expiration)</li>
     * <li>HMAC-SHA signature using the secret key</li>
     * </ul>
     * 
     * @param claims  additional claims to include in the token
     * @param subject the subject (username) for the token
     * @return a compact, signed JWT token string
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validates a JWT token against the provided user details.
     * 
     * <p>
     * A token is considered valid if:
     * </p>
     * <ul>
     * <li>The username in the token matches the provided user's username</li>
     * <li>The token has not expired</li>
     * </ul>
     * 
     * @param token       the JWT token string to validate
     * @param userDetails the user details to validate against
     * @return true if the token is valid for the given user, false otherwise
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}