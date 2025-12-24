package com.mediaserver.controller;

import com.mediaserver.config.MediaProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/thumbnails")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class ThumbnailController {

    private final MediaProperties properties;

    @GetMapping("/file/{fileName}")
    public ResponseEntity<byte[]> getThumbnail(@PathVariable String fileName) {
        try {
            Path thumbnailPath = Path.of(properties.getStorage().getPath(), "thumbnails", fileName);

            if (!Files.exists(thumbnailPath)) {
                return ResponseEntity.notFound().build();
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
