package com.mediaserver.infrastructure.rest.controller;

import com.mediaserver.application.usecase.movie.*;
import com.mediaserver.entity.Movie;
import com.mediaserver.infrastructure.rest.dto.CacheStatsDto;
import com.mediaserver.infrastructure.rest.dto.MovieRequestDto;
import com.mediaserver.infrastructure.rest.dto.MovieResponseDto;
import com.mediaserver.infrastructure.rest.mapper.MovieRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final GetAllMoviesUseCase getAllMoviesUseCase;
    private final GetMovieUseCase getMovieUseCase;
    private final SearchMoviesUseCase searchMoviesUseCase;
    private final GetMoviesByCategoryUseCase getMoviesByCategoryUseCase;
    private final GetReadyMoviesUseCase getReadyMoviesUseCase;
    private final CreateMovieUseCase createMovieUseCase;
    private final UpdateMovieUseCase updateMovieUseCase;
    private final DeleteMovieUseCase deleteMovieUseCase;
    private final StartDownloadUseCase startDownloadUseCase;
    private final GetCacheStatsUseCase getCacheStatsUseCase;
    private final GetCachedMoviesUseCase getCachedMoviesUseCase;
    private final ClearMovieCacheUseCase clearMovieCacheUseCase;
    private final ClearAllCacheUseCase clearAllCacheUseCase;
    private final MovieRestMapper movieMapper;

    @GetMapping
    public ResponseEntity<List<MovieResponseDto>> getAllMovies(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categoryId,
            @RequestParam(defaultValue = "false") boolean readyOnly) {

        var movies = getMovies(search, categoryId, readyOnly);
        var response = movies.stream()
                .map(movieMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    private List<Movie> getMovies(String search, String categoryId, boolean readyOnly) {
        if (search != null && !search.isBlank()) {
            return searchMoviesUseCase.searchMovies(search);
        }
        if (categoryId != null && !categoryId.isBlank()) {
            return getMoviesByCategoryUseCase.getMoviesByCategory(categoryId);
        }
        if (readyOnly) {
            return getReadyMoviesUseCase.getReadyMovies();
        }
        return getAllMoviesUseCase.getAllMovies();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponseDto> getMovie(@PathVariable String id) {
        var movie = getMovieUseCase.getMovie(id);
        return ResponseEntity.ok(movieMapper.toResponse(movie));
    }

    @PostMapping
    public ResponseEntity<MovieResponseDto> createMovie(@Valid @RequestBody MovieRequestDto request) {
        var movie = createMovieUseCase.createMovie(movieMapper.toCreateCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(movieMapper.toResponse(movie));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovieResponseDto> updateMovie(
            @PathVariable String id,
            @Valid @RequestBody MovieRequestDto request) {
        var movie = updateMovieUseCase.updateMovie(movieMapper.toUpdateCommand(id, request));
        return ResponseEntity.ok(movieMapper.toResponse(movie));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable String id) {
        deleteMovieUseCase.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/download")
    public ResponseEntity<Void> startDownload(@PathVariable String id) {
        startDownloadUseCase.startDownload(id);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/cache/stats")
    public ResponseEntity<CacheStatsDto> getCacheStats() {
        return ResponseEntity.ok(getCacheStatsUseCase.getCacheStats());
    }

    @GetMapping("/cached")
    public ResponseEntity<List<MovieResponseDto>> getCachedMovies() {
        var movies = getCachedMoviesUseCase.getCachedMovies();
        var response = movies.stream()
                .map(movieMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/cache")
    public ResponseEntity<Void> clearMovieCache(@PathVariable String id) {
        clearMovieCacheUseCase.clearCache(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/cache")
    public ResponseEntity<Integer> clearAllCache() {
        int cleared = clearAllCacheUseCase.clearAllCache();
        return ResponseEntity.ok(cleared);
    }
}
