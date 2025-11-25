package com.streamletz.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a user in the Streamletz music streaming application.
 * 
 * <p>
 * This class stores user account information including authentication
 * credentials,
 * profile details, and authorization roles. It supports Spring Security
 * integration
 * and manages user access to the application's features.
 * </p>
 * 
 * <p>
 * The entity enforces unique constraints on both username and email to ensure
 * that each user has a distinct identity in the system.
 * </p>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * Unique identifier for the user.
     * Auto-generated using database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique username for the user account.
     * 
     * <p>
     * Must be between 3 and 50 characters and unique across all users.
     * Used for user authentication and display purposes.
     * </p>
     */
    @NotBlank
    @Size(min = 3, max = 50)
    @Column(nullable = false, unique = true)
    private String username;

    /**
     * Email address of the user.
     * 
     * <p>
     * Must be a valid email format and unique across all users.
     * Used for account recovery and communication.
     * </p>
     */
    @NotBlank
    @Size(max = 100)
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Encrypted password for user authentication.
     * 
     * <p>
     * Must be at least 6 characters before encryption.
     * Stored in encrypted format using BCrypt or similar hashing algorithm.
     * </p>
     */
    @NotBlank
    @Size(min = 6, max = 100)
    @Column(nullable = false)
    private String password;

    /**
     * URL or path to the user's profile image.
     * 
     * <p>
     * Optional field that can store either a local file path or a URL
     * to an external image resource. Maximum length of 500 characters.
     * </p>
     */
    @Column(length = 500)
    private String profileImage;

    /**
     * Set of role names assigned to this user for authorization.
     * 
     * <p>
     * Eagerly fetched to support Spring Security authentication.
     * Roles are stored in a separate table (user_roles) to support
     * multiple roles per user.
     * </p>
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    /**
     * Timestamp when the user account was created.
     * 
     * <p>
     * Automatically set by Hibernate when the entity is first persisted.
     * This field is immutable after creation.
     * </p>
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the user account was last updated.
     * 
     * <p>
     * Automatically updated by Hibernate whenever the entity is modified.
     * </p>
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Flag indicating whether the user account is active.
     * 
     * <p>
     * Defaults to {@code true}. When set to {@code false}, the user
     * will not be able to authenticate or access the application.
     * </p>
     */
    @Column
    private boolean enabled = true;
}