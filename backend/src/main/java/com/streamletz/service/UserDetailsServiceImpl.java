package com.streamletz.service;

import com.streamletz.model.User;
import com.streamletz.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Custom implementation of Spring Security's {@link UserDetailsService}.
 * 
 * <p>
 * This service loads user-specific data during authentication. It retrieves
 * user information from the database and converts it into Spring Security's
 * {@link UserDetails} format for authentication and authorization.
 * </p>
 * 
 * <p>
 * Used by Spring Security's authentication manager to validate credentials
 * and load user authorities (roles) during login.
 * </p>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads user details by username for authentication.
     * 
     * <p>
     * This method is called by Spring Security during authentication to retrieve
     * user information. It converts the application's {@link User} entity into
     * Spring Security's {@link UserDetails} format.
     * </p>
     * 
     * @param username the username to look up
     * @return UserDetails object containing user information and authorities
     * @throws UsernameNotFoundException if the user is not found in the database
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                getAuthorities(user));
    }

    /**
     * Converts user roles to Spring Security authorities.
     * 
     * <p>
     * Transforms the user's role strings (e.g., "ROLE_USER", "ROLE_ADMIN")
     * into {@link GrantedAuthority} objects required by Spring Security.
     * </p>
     * 
     * @param user the user whose roles should be converted
     * @return collection of granted authorities for the user
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return user.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}