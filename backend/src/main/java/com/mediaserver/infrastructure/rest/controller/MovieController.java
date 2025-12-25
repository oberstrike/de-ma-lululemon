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

        List<Movie> movies;

        if (search != null && !search.isBlank()) {
            movies = searchMoviesUseCase.searchMovies(search);
        } else if (categoryId != null && !categoryId.isBlank()) {
            movies = getMoviesByCategoryUseCase.getMoviesByCategory(categoryId);
        } else if (readyOnly) {
            movies = getReadyMoviesUseCase.getReadyMovies();
        } else {
            movies = getAllMoviesUseCase.getAllMovies();
        }

        List<MovieResponseDto> response = movies.stream()
                .map(movieMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponseDto> getMovie(@PathVariable String id) {
        Movie movie = getMovieUseCase.getMovie(id);
        return ResponseEntity.ok(movieMapper.toResponse(movie));
    }

    @PostMapping
    public ResponseEntity<MovieResponseDto> createMovie(@Valid @RequestBody MovieRequestDto request) {
        Movie movie = createMovieUseCase.createMovie(movieMapper.toCreateCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(movieMapper.toResponse(movie));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovieResponseDto> updateMovie(
            @PathVariable String id,
            @Valid @RequestBody MovieRequestDto request) {
        Movie movie = updateMovieUseCase.updateMovie(movieMapper.toUpdateCommand(id, request));
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
        List<Movie> movies = getCachedMoviesUseCase.getCachedMovies();
        List<MovieResponseDto> response = movies.stream()
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
