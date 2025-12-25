package com.mediaserver.application.usecase.movie;

import com.mediaserver.application.command.CreateMovieCommand;
import com.mediaserver.domain.model.Movie;

public interface CreateMovieUseCase {
    Movie createMovie(CreateMovieCommand command);
}
