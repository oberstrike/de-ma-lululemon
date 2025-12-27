package com.mediaserver.application.port.out;

import com.mediaserver.domain.model.Movie;
import com.mediaserver.domain.model.MovieStatus;

import java.util.List;
import java.util.Optional;

/**
 * Output port for movie persistence operations. This port will be implemented by the persistence
 * adapter.
 */
public interface MoviePort {

    /**
     * Finds a movie by its ID.
     *
     * @param id the movie ID
     * @return optional containing the movie if found
     */
    Optional<Movie> findById(String id);

    /**
     * Finds all movies.
     *
     * @return list of all movies
     */
    List<Movie> findAll();

    /**
     * Finds movies by status.
     *
     * @param status the movie status
     * @return list of movies with the given status
     */
    List<Movie> findByStatus(MovieStatus status);

    /**
     * Finds movies by category ID.
     *
     * @param categoryId the category ID
     * @return list of movies in the category
     */
    List<Movie> findByCategoryId(String categoryId);

    /**
     * Searches movies by query (title or description).
     *
     * @param query the search query
     * @return list of matching movies
     */
    List<Movie> search(String query);

    /**
     * Finds all ready movies ordered by creation date.
     *
     * @return list of ready movies
     */
    List<Movie> findReadyMovies();

    /**
     * Finds all cached movies (with local path).
     *
     * @return list of cached movies
     */
    List<Movie> findCachedMovies();

    /**
     * Gets total cache size in bytes.
     *
     * @return total size of cached movies
     */
    long getTotalCacheSize();

    /**
     * Counts movies that have a local path (cached).
     *
     * @return count of cached movies
     */
    long countCachedMovies();

    /**
     * Saves a movie.
     *
     * @param movie the movie to save
     * @return the saved movie
     */
    Movie save(Movie movie);

    /**
     * Deletes a movie.
     *
     * @param movie the movie to delete
     */
    void delete(Movie movie);

    List<Movie> findFavorites(String userId);

    void addFavorite(String movieId, String userId);

    void removeFavorite(String movieId, String userId);

    boolean isFavorite(String movieId, String userId);

    /**
     * Applies favorite status to a list of movies for the given user.
     *
     * @param movies the movies to update
     * @param userId the user ID to check favorites for
     * @return movies with favorite flag set appropriately
     */
    List<Movie> applyFavoriteStatus(List<Movie> movies, String userId);

    /**
     * Finds cached movies that are not favorites (for cache clearing).
     *
     * @return list of cached non-favorite movies
     */
    List<Movie> findCachedNonFavorites();
}
