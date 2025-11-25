package com.streamletz.repository;

import com.streamletz.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing {@link User} entities.
 * 
 * <p>
 * Extends Spring Data JPA's {@code JpaRepository} to provide CRUD operations
 * and custom query methods for user management, including username and email
 * lookups and existence checks.
 * </p>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their unique username.
     * 
     * @param username the username to search for
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their unique email address.
     * 
     * @param email the email address to search for
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user with the given username exists.
     * 
     * @param username the username to check
     * @return true if a user with this username exists, false otherwise
     */
    Boolean existsByUsername(String username);

    /**
     * Checks if a user with the given email exists.
     * 
     * @param email the email to check
     * @return true if a user with this email exists, false otherwise
     */
    Boolean existsByEmail(String email);
}