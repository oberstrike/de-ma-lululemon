package com.mediaserver.infrastructure.rest.controller;

import com.mediaserver.config.MediaProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/thumbnails")
@RequiredArgsConstructor
@Slf4j
public class ThumbnailController {

    private final MediaProperties properties;

    @GetMapping("/file/{fileName}")
    public ResponseEntity<byte[]> getThumbnail(@PathVariable String fileName) {
        try {
            if (fileName == null
                    || fileName.contains("..")
                    || fileName.contains("/")
                    || fileName.contains("\\")) {
                log.warn("Rejected potentially malicious filename: {}", fileName);
                return ResponseEntity.badRequest().build();
            }

            String lowerName = fileName.toLowerCase();
            if (!lowerName.endsWith(".png")
                    && !lowerName.endsWith(".jpg")
                    && !lowerName.endsWith(".jpeg")
                    && !lowerName.endsWith(".gif")
                    && !lowerName.endsWith(".webp")) {
                log.warn("Rejected non-image file request: {}", fileName);
                return ResponseEntity.badRequest().build();
            }

            Path thumbnailDir = Path.of(properties.getStorage().getPath(), "thumbnails");
            Path thumbnailPath = thumbnailDir.resolve(fileName).normalize();

            if (!thumbnailPath.startsWith(thumbnailDir)) {
                log.warn("Path traversal attempt detected: {}", fileName);
                return ResponseEntity.badRequest().build();
            }

            if (!Files.exists(thumbnailPath)) {
                return ResponseEntity.notFound().build();
            }

            long fileSize = Files.size(thumbnailPath);
            if (fileSize > 10 * 1024 * 1024) {
                log.warn("Thumbnail file too large: {} ({} bytes)", fileName, fileSize);
                return ResponseEntity.badRequest().build();
            }

            byte[] imageBytes = Files.readAllBytes(thumbnailPath);
            MediaType mediaType = getMediaType(fileName);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
                    .body(imageBytes);

        } catch (IOException e) {
            log.error("Error serving thumbnail {}: {}", fileName, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    private MediaType getMediaType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
        if (lower.endsWith(".gif")) return MediaType.IMAGE_GIF;
        if (lower.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        return MediaType.IMAGE_PNG;
    }
}
