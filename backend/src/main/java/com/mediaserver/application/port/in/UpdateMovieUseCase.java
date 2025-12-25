package com.mediaserver.application.port.in;

import com.mediaserver.application.command.UpdateMovieCommand;
import com.mediaserver.domain.model.Movie;

/** Use case for updating an existing movie. Defines the input port for movie updates. */
public interface UpdateMovieUseCase {

    /**
     * Updates an existing movie with the provided data.
     *
     * @param id the movie ID
     * @param command the movie update command
     * @return the updated movie
     * @throws com.mediaserver.exception.MovieNotFoundException if movie not found
     * @throws com.mediaserver.exception.CategoryNotFoundException if category not found
     */
    Movie updateMovie(String id, UpdateMovieCommand command);
}
