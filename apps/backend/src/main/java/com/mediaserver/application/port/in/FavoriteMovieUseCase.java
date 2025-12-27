package com.mediaserver.application.port.in;

import com.mediaserver.domain.model.Movie;

import java.util.List;

/**
 * Use case for managing movie favorites. Favorites are protected from cache clearing operations.
 */
public interface FavoriteMovieUseCase {

    /**
     * Marks a movie as favorite.
     *
     * @param movieId the movie ID
     * @return the updated movie
     */
    Movie addFavorite(String movieId);

    /**
     * Removes a movie from favorites.
     *
     * @param movieId the movie ID
     * @return the updated movie
     */
    Movie removeFavorite(String movieId);

    /**
     * Gets all favorite movies.
     *
     * @return list of favorite movies
     */
    List<Movie> getFavorites();
}
