package com.mediaserver.application.port.in;

import com.mediaserver.domain.model.Movie;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/** Use case for managing the movie cache. Defines the input port for cache operations. */
public interface CacheManagementUseCase {

    /**
     * Retrieves cache statistics.
     *
     * @return cache stats including size and usage
     */
    CacheStats getCacheStats();

    /**
     * Retrieves all cached movies.
     *
     * @return list of cached movies
     */
    List<Movie> getCachedMovies();

    /**
     * Clears the cache for a specific movie.
     *
     * @param movieId the movie ID
     * @throws com.mediaserver.exception.MovieNotFoundException if movie not found
     */
    void clearCache(String movieId);

    /**
     * Clears the cache for all movies.
     *
     * @return number of movies cleared
     */
    int clearAllCache();

    /** Cache statistics value object. */
    @Value
    @Builder
    class CacheStats {
        long totalSizeBytes;
        long maxSizeBytes;
        int usagePercent;
        long movieCount;
    }
}
