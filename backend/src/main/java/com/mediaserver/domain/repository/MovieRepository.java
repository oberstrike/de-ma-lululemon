package com.mediaserver.domain.repository;

import com.mediaserver.domain.model.Movie;
import com.mediaserver.domain.model.MovieStatus;
import java.util.List;
import java.util.Optional;

/**
 * Repository port interface for Movie domain entity. This is a port in hexagonal architecture -
 * implementations are adapters.
 */
public interface MovieRepository {

    /**
     * Find a movie by its ID.
     *
     * @param id the movie ID
     * @return Optional containing the movie if found
     */
    Optional<Movie> findById(String id);

    /**
     * Find all movies.
     *
     * @return list of all movies
     */
    List<Movie> findAll();

    /**
     * Save a movie (create or update).
     *
     * @param movie the movie to save
     * @return the saved movie
     */
    Movie save(Movie movie);

    /**
     * Delete a movie by its ID.
     *
     * @param id the movie ID
     */
    void delete(String id);

    /**
     * Find movies by status.
     *
     * @param status the movie status
     * @return list of movies with the given status
     */
    List<Movie> findByStatus(MovieStatus status);

    /**
     * Find movies by category ID.
     *
     * @param categoryId the category ID
     * @return list of movies in the category
     */
    List<Movie> findByCategoryId(String categoryId);

    /**
     * Search movies by title (case-insensitive partial match).
     *
     * @param query the search query
     * @return list of matching movies
     */
    List<Movie> search(String query);

    /**
     * Find ready movies ordered by creation date descending.
     *
     * @return list of ready movies
     */
    List<Movie> findReadyMovies();

    /**
     * Find all cached movies (have local path) ordered by update date descending.
     *
     * @return list of cached movies
     */
    List<Movie> findCachedMovies();

    /**
     * Get total size of all cached movies in bytes.
     *
     * @return total cache size
     */
    Long getTotalCacheSize();

    /**
     * Count number of cached movies.
     *
     * @return count of cached movies
     */
    long countCached();

    /**
     * Check if a movie exists with the given Mega path.
     *
     * @param megaPath the Mega path
     * @return true if a movie exists with the given path
     */
    boolean existsByMegaPath(String megaPath);

    /**
     * Find all favorite movies.
     *
     * @return list of favorite movies
     */
    List<Movie> findFavorites();

    /**
     * Find cached movies that are not favorites (for cache clearing).
     *
     * @return list of cached non-favorite movies
     */
    List<Movie> findCachedNonFavorites();
}
