package com.mediaserver.controller;

import com.mediaserver.entity.Movie;
import com.mediaserver.exception.MovieNotFoundException;
import com.mediaserver.repository.MovieRepository;
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

    private final MovieRepository movieRepository;

    @GetMapping("/{movieId}")
    public ResponseEntity<byte[]> getThumbnail(@PathVariable String movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException(movieId));

        String thumbnailPath = movie.getThumbnailUrl();
        if (thumbnailPath == null || thumbnailPath.isBlank()) {
            return ResponseEntity.notFound().build();
        }

        try {
            // Create temp file for thumbnail
            Path tempFile = Files.createTempFile("thumb_" + movieId, getExtension(thumbnailPath));

            // Download from Mega
            ProcessBuilder pb = new ProcessBuilder("mega-get", thumbnailPath, tempFile.toString());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                log.error("Failed to download thumbnail from Mega: {}", thumbnailPath);
                Files.deleteIfExists(tempFile);
                return ResponseEntity.notFound().build();
            }

            byte[] imageBytes = Files.readAllBytes(tempFile);
            Files.deleteIfExists(tempFile);

            MediaType mediaType = getMediaType(thumbnailPath);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                    .body(imageBytes);

        } catch (IOException | InterruptedException e) {
            log.error("Error fetching thumbnail for movie {}: {}", movieId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    private String getExtension(String path) {
        int lastDot = path.lastIndexOf('.');
        return lastDot > 0 ? path.substring(lastDot) : ".png";
    }

    private MediaType getMediaType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
        if (lower.endsWith(".gif")) return MediaType.IMAGE_GIF;
        if (lower.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        return MediaType.IMAGE_PNG;
    }
}
