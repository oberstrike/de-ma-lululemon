package com.mediaserver.infrastructure.rest.controller;

import com.mediaserver.application.usecase.movie.*;
import com.mediaserver.domain.model.Movie;
import com.mediaserver.infrastructure.rest.dto.CacheStatsDto;
import com.mediaserver.infrastructure.rest.dto.MovieRequestDto;
import com.mediaserver.infrastructure.rest.dto.MovieResponseDto;
import com.mediaserver.infrastructure.rest.mapper.MovieRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    private final AddFavoriteUseCase addFavoriteUseCase;
    private final RemoveFavoriteUseCase removeFavoriteUseCase;
    private final GetFavoritesUseCase getFavoritesUseCase;
    private final MovieRestMapper movieMapper;

    @GetMapping
    public List<MovieResponseDto> getAllMovies(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categoryId,
            @RequestParam(defaultValue = "false") boolean readyOnly) {
        return getMovies(search, categoryId, readyOnly).stream()
                .map(movieMapper::toResponse)
                .toList();
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
    public MovieResponseDto getMovie(@PathVariable String id) {
        return movieMapper.toResponse(getMovieUseCase.getMovie(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovieResponseDto createMovie(@Valid @RequestBody MovieRequestDto request) {
        var movie = createMovieUseCase.createMovie(movieMapper.toCreateCommand(request));
        return movieMapper.toResponse(movie);
    }

    @PutMapping("/{id}")
    public MovieResponseDto updateMovie(
            @PathVariable String id,
            @Valid @RequestBody MovieRequestDto request) {
        var movie = updateMovieUseCase.updateMovie(movieMapper.toUpdateCommand(id, request));
        return movieMapper.toResponse(movie);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMovie(@PathVariable String id) {
        deleteMovieUseCase.deleteMovie(id);
    }

    @PostMapping("/{id}/download")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void startDownload(@PathVariable String id) {
        startDownloadUseCase.startDownload(id);
    }

    @GetMapping("/cache/stats")
    public CacheStatsDto getCacheStats() {
        return getCacheStatsUseCase.getCacheStats();
    }

    @GetMapping("/cached")
    public List<MovieResponseDto> getCachedMovies() {
        return getCachedMoviesUseCase.getCachedMovies().stream()
                .map(movieMapper::toResponse)
                .toList();
    }

    @DeleteMapping("/{id}/cache")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearMovieCache(@PathVariable String id) {
        clearMovieCacheUseCase.clearCache(id);
    }

    @DeleteMapping("/cache")
    public int clearAllCache() {
        return clearAllCacheUseCase.clearAllCache();
    }

    @GetMapping("/favorites")
    public List<MovieResponseDto> getFavorites() {
        return getFavoritesUseCase.getFavorites().stream()
                .map(movieMapper::toResponse)
                .toList();
    }

    @PostMapping("/{id}/favorite")
    public MovieResponseDto addFavorite(@PathVariable String id) {
        return movieMapper.toResponse(addFavoriteUseCase.addFavorite(id));
    }

    @DeleteMapping("/{id}/favorite")
    public MovieResponseDto removeFavorite(@PathVariable String id) {
        return movieMapper.toResponse(removeFavoriteUseCase.removeFavorite(id));
    }
}
