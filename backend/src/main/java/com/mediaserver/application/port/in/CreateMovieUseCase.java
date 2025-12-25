package com.mediaserver.application.port.in;

import com.mediaserver.application.command.CreateMovieCommand;
import com.mediaserver.domain.model.Movie;

/** Use case for creating a new movie. Defines the input port for movie creation. */
public interface CreateMovieUseCase {

    /**
     * Creates a new movie with the provided data.
     *
     * @param command the movie creation command
     * @return the created movie
     * @throws com.mediaserver.exception.CategoryNotFoundException if category not found
     */
    Movie createMovie(CreateMovieCommand command);
}
