package com.streamletz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Streamletz music streaming platform.
 * 
 * <p>
 * This is the entry point for the Spring Boot application. It bootstraps
 * the entire application context, including:
 * </p>
 * <ul>
 * <li>REST API controllers for music streaming and user management</li>
 * <li>JWT-based authentication and authorization</li>
 * <li>JPA repositories for data persistence</li>
 * <li>Service layer for business logic</li>
 * <li>Security configuration with Spring Security</li>
 * <li>OpenAPI/Swagger documentation</li>
 * </ul>
 * 
 * <p>
 * The application provides a complete music streaming backend with features
 * such as user authentication, track management, playlist creation, and audio
 * streaming with HTTP Range support.
 * </p>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
@SpringBootApplication
public class StreamletzApplication {

    /**
     * Main method that starts the Spring Boot application.
     * 
     * <p>
     * This method delegates to Spring Boot's {@link SpringApplication#run}
     * to launch the embedded web server and initialize the application context.
     * </p>
     * 
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(StreamletzApplication.class, args);
    }
}