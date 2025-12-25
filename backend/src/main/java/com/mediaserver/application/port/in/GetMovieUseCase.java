package com.mediaserver.application.port.in;

import com.mediaserver.domain.model.Movie;
import java.util.List;

/**
 * Use case for retrieving movie information.
 * Defines the input port for movie queries.
 */
public interface GetMovieUseCase {

    /**
     * Retrieves a single movie by its ID.
     * @param id the movie ID
     * @return the movie
     * @throws com.mediaserver.exception.MovieNotFoundException if movie not found
     */
    Movie getMovie(String id);

    /**
     * Retrieves all movies in the system.
     * @return list of all movies
     */
    List<Movie> getAllMovies();

    /**
     * Retrieves all movies that are ready to stream (status = READY).
     * @return list of ready movies
     */
    List<Movie> getReadyMovies();

    /**
     * Retrieves all movies in a specific category.
     * @param categoryId the category ID
     * @return list of movies in the category
     */
    List<Movie> getMoviesByCategory(String categoryId);
}
