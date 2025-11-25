package com.streamletz.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * REST controller for serving album and track cover art images.
 * 
 * <p>
 * This controller provides a public endpoint for retrieving cover art images
 * stored on the server's file system. It automatically detects the content type
 * and serves images with appropriate headers.
 * </p>
 * 
 * <p>
 * The endpoint is publicly accessible to allow cover art display without
 * authentication.
 * </p>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/covers")
@Slf4j
public class CoverController {

    @Value("${music.covers.path:/covers}")
    private String coversPath;

    /**
     * Retrieves a cover art image by filename.
     * 
     * <p>
     * Serves image files from the configured covers directory. The content type
     * is automatically detected from the file, defaulting to JPEG if detection
     * fails.
     * </p>
     * 
     * <p>
     * This endpoint is publicly accessible to allow cover art display in the UI
     * without requiring authentication.
     * </p>
     * 
     * @param filename the name of the cover image file to retrieve
     * @return ResponseEntity containing the image resource with appropriate content
     *         type,
     *         or HTTP 404 if the file is not found,
     *         or HTTP 500 if an error occurs reading the file
     */
    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getCover(@PathVariable String filename) {
        try {
            Path coverPath = Paths.get(coversPath, filename);

            if (!Files.exists(coverPath)) {
                log.debug("Cover not found: {}", filename);
                return ResponseEntity.notFound().build();
            }

            @SuppressWarnings("null")
            Resource resource = new FileSystemResource(coverPath);

            String contentType = Files.probeContentType(coverPath);
            if (contentType == null) {
                contentType = MediaType.IMAGE_JPEG_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (Exception e) {
            log.error("Error serving cover {}: {}", filename, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}