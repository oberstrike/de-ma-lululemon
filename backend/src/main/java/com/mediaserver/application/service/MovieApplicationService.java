package com.mediaserver.application.service;

import com.mediaserver.application.command.CreateMovieCommand;
import com.mediaserver.application.command.UpdateMovieCommand;
import com.mediaserver.application.port.in.*;
import com.mediaserver.application.port.out.CategoryPort;
import com.mediaserver.application.port.out.DownloadServicePort;
import com.mediaserver.application.port.out.FileStoragePort;
import com.mediaserver.application.port.out.MoviePort;
import com.mediaserver.application.usecase.movie.AddFavoriteUseCase;
import com.mediaserver.application.usecase.movie.GetFavoritesUseCase;
import com.mediaserver.application.usecase.movie.RemoveFavoriteUseCase;
import com.mediaserver.config.MediaProperties;
import com.mediaserver.domain.model.Category;
import com.mediaserver.domain.model.Movie;
import com.mediaserver.domain.model.MovieStatus;
import com.mediaserver.exception.CategoryNotFoundException;
import com.mediaserver.exception.MovieNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service implementing movie-related use cases.
 * This service orchestrates the business logic and delegates to output ports.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MovieApplicationService implements
        GetMovieUseCase,
        CreateMovieUseCase,
        UpdateMovieUseCase,
        DeleteMovieUseCase,
        SearchMoviesUseCase,
        DownloadMovieUseCase,
        CacheManagementUseCase,
        FavoriteMovieUseCase,
        AddFavoriteUseCase,
        RemoveFavoriteUseCase,
        GetFavoritesUseCase {

    private final MoviePort moviePort;
    private final CategoryPort categoryPort;
    private final FileStoragePort fileStoragePort;
    private final DownloadServicePort downloadServicePort;
    private final MediaProperties properties;

    // Thread-safe set to track movies currently being downloaded
    private static final Set<String> activeDownloads = ConcurrentHashMap.newKeySet();

    @Override
    public Movie getMovie(String id) {
        return moviePort.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));
    }

    @Override
    public List<Movie> getAllMovies() {
        return moviePort.findAll();
    }

    @Override
    public List<Movie> getReadyMovies() {
        return moviePort.findReadyMovies();
    }

    @Override
    public List<Movie> getMoviesByCategory(String categoryId) {
        return moviePort.findByCategoryId(categoryId);
    }

    @Override
    public List<Movie> searchMovies(String query) {
        return moviePort.search(query);
    }

    @Override
    public Movie createMovie(CreateMovieCommand command) {
        Movie.MovieBuilder movieBuilder = Movie.builder()
                .title(command.getTitle())
                .description(command.getDescription())
                .year(command.getYear())
                .duration(command.getDuration())
                .megaUrl(command.getMegaUrl())
                .thumbnailUrl(command.getThumbnailUrl())
                .status(MovieStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now());

        // Set category if provided
        if (command.getCategoryId() != null) {
            Category category = categoryPort.findById(command.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(command.getCategoryId()));
            movieBuilder.categoryId(category.getId());
        }

        Movie movie = movieBuilder.build();
        return moviePort.save(movie);
    }

    @Override
    public Movie updateMovie(String id, UpdateMovieCommand command) {
        Movie movie = moviePort.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));

        // Check if mega URL changed, if so reset download status
        boolean megaUrlChanged = command.getMegaUrl() != null &&
                !command.getMegaUrl().equals(movie.getMegaUrl());

        String newCategoryId = movie.getCategoryId();
        if (command.getCategoryId() != null) {
            Category category = categoryPort.findById(command.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(command.getCategoryId()));
            newCategoryId = category.getId();
        }

        Movie updatedMovie = movie.withTitle(command.getTitle())
                .withDescription(command.getDescription())
                .withYear(command.getYear())
                .withDuration(command.getDuration())
                .withThumbnailUrl(command.getThumbnailUrl())
                .withCategoryId(newCategoryId)
                .withUpdatedAt(LocalDateTime.now());

        // If Mega URL changed, reset status and clear local path
        if (megaUrlChanged) {
            updatedMovie = updatedMovie
                    .withMegaUrl(command.getMegaUrl())
                    .withStatus(MovieStatus.PENDING)
                    .withLocalPath(null);
        }

        return moviePort.save(updatedMovie);
    }

    @Override
    public void deleteMovie(String id) {
        Movie movie = moviePort.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));

        if (movie.getLocalPath() != null) {
            try {
                fileStoragePort.deleteIfExists(Path.of(movie.getLocalPath()));
            } catch (IOException e) {
                log.warn("Failed to delete video file: {}", movie.getLocalPath(), e);
            }
        }

        moviePort.delete(movie);
    }

    @Override
    public void startDownload(String movieId) {
        // Use atomic operation to prevent concurrent downloads of the same movie
        if (!activeDownloads.add(movieId)) {
            throw new IllegalStateException("Download already in progress for movie: " + movieId);
        }

        try {
            Movie movie = moviePort.findById(movieId)
                    .orElseThrow(() -> new MovieNotFoundException(movieId));

            if (movie.isCached()) {
                throw new IllegalStateException("Movie is already downloaded");
            }

            if (movie.getStatus() == MovieStatus.DOWNLOADING) {
                throw new IllegalStateException("Movie is already being downloaded");
            }

            Movie downloadingMovie = movie.withStatus(MovieStatus.DOWNLOADING);
            moviePort.save(downloadingMovie);

            // Start async download - the download service should call removeFromActiveDownloads when done
            downloadServicePort.downloadMovie(downloadingMovie)
                    .whenComplete((result, error) -> activeDownloads.remove(movieId));
        } catch (Exception e) {
            // Ensure we remove from active downloads on any error
            activeDownloads.remove(movieId);
            throw e;
        }
    }

    /**
     * Check if a movie is currently being downloaded.
     */
    public boolean isDownloadActive(String movieId) {
        return activeDownloads.contains(movieId);
    }

    @Override
    public CacheStats getCacheStats() {
        var totalSize = moviePort.getTotalCacheSize();
        var maxSize = (long) properties.getStorage().getMaxCacheSizeGb() * 1024 * 1024 * 1024;

        return CacheStats.builder()
                .totalSizeBytes(totalSize)
                .maxSizeBytes(maxSize)
                .usagePercent(maxSize > 0 ? (int) ((totalSize * 100) / maxSize) : 0)
                .movieCount(moviePort.countCachedMovies())
                .build();
    }

    @Override
    public List<Movie> getCachedMovies() {
        return moviePort.findCachedMovies();
    }

    @Override
    public void clearCache(String movieId) {
        Movie movie = moviePort.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException(movieId));

        if (movie.getLocalPath() == null) {
            log.info("Movie {} has no cached file", movieId);
            return;
        }

        try {
            Path localFile = Path.of(movie.getLocalPath());
            if (fileStoragePort.deleteIfExists(localFile)) {
                log.info("Deleted cached file: {}", localFile);
            }
        } catch (IOException e) {
            log.error("Failed to delete cached file for movie {}: {}", movieId, e.getMessage());
            throw new RuntimeException("Failed to delete cached file", e);
        }

        Movie clearedMovie = movie.withLocalPath(null)
                .withFileSize(null)
                .withStatus(MovieStatus.PENDING)
                .withUpdatedAt(LocalDateTime.now());

        moviePort.save(clearedMovie);
        log.info("Cleared cache for movie: {} ({})", movie.getTitle(), movieId);
    }

    @Override
    public int clearAllCache() {
        // Only clear non-favorite movies
        List<Movie> cachedMovies = moviePort.findCachedNonFavorites();
        int cleared = 0;

        for (Movie movie : cachedMovies) {
            if (movie.getLocalPath() == null) {
                continue;
            }

            try {
                fileStoragePort.deleteIfExists(Path.of(movie.getLocalPath()));

                Movie clearedMovie = movie.withLocalPath(null)
                        .withFileSize(null)
                        .withStatus(MovieStatus.PENDING)
                        .withUpdatedAt(LocalDateTime.now());

                moviePort.save(clearedMovie);
                cleared++;
            } catch (IOException e) {
                log.warn("Failed to delete cached file for movie {}: {}", movie.getId(), e.getMessage());
            }
        }

        log.info("Cleared cache for {} movies (favorites preserved)", cleared);
        return cleared;
    }

    // FavoriteMovieUseCase implementation

    @Override
    public Movie addFavorite(String movieId) {
        Movie movie = moviePort.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException(movieId));

        if (movie.isFavorite()) {
            log.info("Movie {} is already a favorite", movieId);
            return movie;
        }

        Movie updatedMovie = movie.withFavorite(true)
                .withUpdatedAt(LocalDateTime.now());

        log.info("Added movie to favorites: {} ({})", movie.getTitle(), movieId);
        return moviePort.save(updatedMovie);
    }

    @Override
    public Movie removeFavorite(String movieId) {
        Movie movie = moviePort.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException(movieId));

        if (!movie.isFavorite()) {
            log.info("Movie {} is not a favorite", movieId);
            return movie;
        }

        Movie updatedMovie = movie.withFavorite(false)
                .withUpdatedAt(LocalDateTime.now());

        log.info("Removed movie from favorites: {} ({})", movie.getTitle(), movieId);
        return moviePort.save(updatedMovie);
    }

    @Override
    public List<Movie> getFavorites() {
        return moviePort.findFavorites();
    }
}
