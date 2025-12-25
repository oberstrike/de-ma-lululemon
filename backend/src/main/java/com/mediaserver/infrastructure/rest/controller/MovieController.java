package com.mediaserver.infrastructure.rest.controller;

import com.mediaserver.application.usecase.movie.*;
import com.mediaserver.domain.model.Movie;
import com.mediaserver.infrastructure.rest.dto.CacheStatsDTO;
import com.mediaserver.infrastructure.rest.dto.MovieRequestDTO;
import com.mediaserver.infrastructure.rest.dto.MovieResponseDTO;
import com.mediaserver.infrastructure.rest.mapper.MovieRestMapper;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
    public List<MovieResponseDTO> getAllMovies(
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
    public MovieResponseDTO getMovie(@PathVariable String id) {
        return movieMapper.toResponse(getMovieUseCase.getMovie(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovieResponseDTO createMovie(@Valid @RequestBody MovieRequestDTO request) {
        var movie = createMovieUseCase.createMovie(movieMapper.toCreateCommand(request));
        return movieMapper.toResponse(movie);
    }

    @PutMapping("/{id}")
    public MovieResponseDTO updateMovie(
            @PathVariable String id, @Valid @RequestBody MovieRequestDTO request) {
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
    public CacheStatsDTO getCacheStats() {
        var stats = getCacheStatsUseCase.getCacheStats();
        return CacheStatsDTO.builder()
                .totalSizeBytes(stats.getTotalSizeBytes())
                .maxSizeBytes(stats.getMaxSizeBytes())
                .usagePercent(stats.getUsagePercent())
                .movieCount(stats.getMovieCount())
                .build();
    }

    @GetMapping("/cached")
    public List<MovieResponseDTO> getCachedMovies() {
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
    public List<MovieResponseDTO> getFavorites() {
        return getFavoritesUseCase.getFavorites().stream().map(movieMapper::toResponse).toList();
    }

    @PostMapping("/{id}/favorite")
    public MovieResponseDTO addFavorite(@PathVariable String id) {
        return movieMapper.toResponse(addFavoriteUseCase.addFavorite(id));
    }

    @DeleteMapping("/{id}/favorite")
    public MovieResponseDTO removeFavorite(@PathVariable String id) {
        return movieMapper.toResponse(removeFavoriteUseCase.removeFavorite(id));
    }
}
