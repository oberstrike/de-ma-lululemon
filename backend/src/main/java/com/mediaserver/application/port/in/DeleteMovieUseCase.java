package com.mediaserver.application.port.in;

/**
 * Use case for deleting a movie.
 * Defines the input port for movie deletion.
 */
public interface DeleteMovieUseCase {

    /**
     * Deletes a movie and its associated local file if exists.
     * @param id the movie ID
     * @throws com.mediaserver.exception.MovieNotFoundException if movie not found
     */
    void deleteMovie(String id);
}
