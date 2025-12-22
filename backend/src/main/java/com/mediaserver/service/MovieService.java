package com.mediaserver.service;

import com.mediaserver.config.MediaProperties;
import com.mediaserver.dto.CacheStatsDto;
import com.mediaserver.dto.MovieCreateRequest;
import com.mediaserver.dto.MovieDto;
import com.mediaserver.dto.MovieMapper;
import com.mediaserver.entity.Category;
import com.mediaserver.entity.Movie;
import com.mediaserver.entity.MovieStatus;
import com.mediaserver.exception.CategoryNotFoundException;
import com.mediaserver.exception.MovieNotFoundException;
import com.mediaserver.repository.CategoryRepository;
import com.mediaserver.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MovieService {

    private final MovieRepository movieRepository;
    private final CategoryRepository categoryRepository;
    private final MegaDownloadService downloadService;
    private final MovieMapper movieMapper;
    private final MediaProperties properties;

    public List<MovieDto> getAllMovies() {
        return movieRepository.findAll().stream().map(movieMapper::toDto).toList();
    }

    public List<MovieDto> getReadyMovies() {
        return movieRepository.findReadyMovies().stream().map(movieMapper::toDto).toList();
    }

    public MovieDto getMovie(String id) {
        return movieRepository.findById(id).map(movieMapper::toDto)
                .orElseThrow(() -> new MovieNotFoundException(id));
    }

    public List<MovieDto> searchMovies(String query) {
        return movieRepository.search(query).stream().map(movieMapper::toDto).toList();
    }

    public List<MovieDto> getMoviesByCategory(String categoryId) {
        return movieRepository.findByCategoryId(categoryId).stream().map(movieMapper::toDto).toList();
    }

    public MovieDto createMovie(MovieCreateRequest request) {
        Movie movie = Movie.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .year(request.getYear())
                .duration(request.getDuration())
                .megaUrl(request.getMegaUrl())
                .thumbnailUrl(request.getThumbnailUrl())
                .status(MovieStatus.PENDING)
                .build();

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.getCategoryId()));
            movie.setCategory(category);
        }

        movie = movieRepository.save(movie);
        return movieMapper.toDto(movie);
    }

    public MovieDto updateMovie(String id, MovieCreateRequest request) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new MovieNotFoundException(id));

        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setYear(request.getYear());
        movie.setDuration(request.getDuration());
        movie.setThumbnailUrl(request.getThumbnailUrl());

        if (request.getMegaUrl() != null && !request.getMegaUrl().equals(movie.getMegaUrl())) {
            movie.setMegaUrl(request.getMegaUrl());
            movie.setStatus(MovieStatus.PENDING);
            movie.setLocalPath(null);
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.getCategoryId()));
            movie.setCategory(category);
        }

        movie = movieRepository.save(movie);
        return movieMapper.toDto(movie);
    }

    public void deleteMovie(String id) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new MovieNotFoundException(id));

        if (movie.getLocalPath() != null) {
            try {
                Files.deleteIfExists(Path.of(movie.getLocalPath()));
            } catch (IOException e) {
                log.warn("Failed to delete video file: {}", movie.getLocalPath(), e);
            }
        }
        movieRepository.delete(movie);
    }

    public void startDownload(String movieId) {
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException(movieId));

        if (movie.isCached()) {
            throw new IllegalStateException("Movie is already downloaded");
        }

        movie.setStatus(MovieStatus.DOWNLOADING);
        movieRepository.save(movie);
        downloadService.downloadMovie(movie);
    }

    public CacheStatsDto getCacheStats() {
        long totalSize = movieRepository.getTotalCacheSize();
        long maxSize = (long) properties.getStorage().getMaxCacheSizeGb() * 1024 * 1024 * 1024;

        return CacheStatsDto.builder()
                .totalSizeBytes(totalSize)
                .maxSizeBytes(maxSize)
                .usagePercent(maxSize > 0 ? (int) ((totalSize * 100) / maxSize) : 0)
                .movieCount(movieRepository.countByLocalPathIsNotNull())
                .build();
    }

    public List<MovieDto> getCachedMovies() {
        return movieRepository.findCachedMovies().stream().map(movieMapper::toDto).toList();
    }

    public void clearCache(String movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException(movieId));

        if (movie.getLocalPath() == null) {
            log.info("Movie {} has no cached file", movieId);
            return;
        }

        try {
            Path localFile = Path.of(movie.getLocalPath());
            if (Files.deleteIfExists(localFile)) {
                log.info("Deleted cached file: {}", localFile);
            }
        } catch (IOException e) {
            log.error("Failed to delete cached file for movie {}: {}", movieId, e.getMessage());
            throw new RuntimeException("Failed to delete cached file", e);
        }

        movie.setLocalPath(null);
        movie.setFileSize(null);
        movie.setStatus(MovieStatus.PENDING);
        movieRepository.save(movie);
        log.info("Cleared cache for movie: {} ({})", movie.getTitle(), movieId);
    }

    public int clearAllCache() {
        List<Movie> cachedMovies = movieRepository.findCachedMovies();
        int cleared = 0;

        for (Movie movie : cachedMovies) {
            try {
                if (movie.getLocalPath() != null) {
                    Files.deleteIfExists(Path.of(movie.getLocalPath()));
                    movie.setLocalPath(null);
                    movie.setFileSize(null);
                    movie.setStatus(MovieStatus.PENDING);
                    movieRepository.save(movie);
                    cleared++;
                }
            } catch (IOException e) {
                log.warn("Failed to delete cached file for movie {}: {}", movie.getId(), e.getMessage());
            }
        }

        log.info("Cleared cache for {} movies", cleared);
        return cleared;
    }
}
