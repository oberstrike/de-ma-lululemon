package com.mediaserver.application.usecase.movie;

import com.mediaserver.application.command.UpdateMovieCommand;
import com.mediaserver.entity.Movie;

public interface UpdateMovieUseCase {
    Movie updateMovie(UpdateMovieCommand command);
}
