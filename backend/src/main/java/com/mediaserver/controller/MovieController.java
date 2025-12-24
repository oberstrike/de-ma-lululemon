package com.mediaserver.controller;

import com.mediaserver.dto.CacheStatsDto;
import com.mediaserver.dto.MovieCreateRequest;
import com.mediaserver.dto.MovieDto;
import com.mediaserver.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public ResponseEntity<List<MovieDto>> getAllMovies(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categoryId,
            @RequestParam(defaultValue = "false") boolean readyOnly) {

        List<MovieDto> movies;

        if (search != null && !search.isBlank()) {
            movies = movieService.searchMovies(search);
        } else if (categoryId != null && !categoryId.isBlank()) {
            movies = movieService.getMoviesByCategory(categoryId);
        } else if (readyOnly) {
            movies = movieService.getReadyMovies();
        } else {
            movies = movieService.getAllMovies();
        }

        return ResponseEntity.ok(movies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieDto> getMovie(@PathVariable String id) {
        return ResponseEntity.ok(movieService.getMovie(id));
    }

    @PostMapping
    public ResponseEntity<MovieDto> createMovie(@Valid @RequestBody MovieCreateRequest request) {
        MovieDto movie = movieService.createMovie(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(movie);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovieDto> updateMovie(
            @PathVariable String id,
            @Valid @RequestBody MovieCreateRequest request) {
        return ResponseEntity.ok(movieService.updateMovie(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable String id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/download")
    public ResponseEntity<Void> startDownload(@PathVariable String id) {
        movieService.startDownload(id);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/cache/stats")
    public ResponseEntity<CacheStatsDto> getCacheStats() {
        return ResponseEntity.ok(movieService.getCacheStats());
    }

    @GetMapping("/cached")
    public ResponseEntity<List<MovieDto>> getCachedMovies() {
        return ResponseEntity.ok(movieService.getCachedMovies());
    }

    @DeleteMapping("/{id}/cache")
    public ResponseEntity<Void> clearMovieCache(@PathVariable String id) {
        movieService.clearCache(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/cache")
    public ResponseEntity<Integer> clearAllCache() {
        int cleared = movieService.clearAllCache();
        return ResponseEntity.ok(cleared);
    }
}
