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

@RestController
@RequestMapping("/api/covers")
@Slf4j
public class CoverController {

    @Value("${music.covers.path:./covers}")
    private String coversPath;

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getCover(@PathVariable String filename) {
        try {
            Path coverPath = Paths.get(coversPath, filename);

            if (!Files.exists(coverPath)) {
                log.warn("Cover not found: {}", filename);
                return ResponseEntity.notFound().build();
            }

            @SuppressWarnings("null")
            Resource resource = new FileSystemResource(coverPath);

            // Determine content type based on file extension
            String contentType = Files.probeContentType(coverPath);
            if (contentType == null) {
                contentType = MediaType.IMAGE_JPEG_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (Exception e) {
            log.error("Error serving cover {}: {}", filename, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}