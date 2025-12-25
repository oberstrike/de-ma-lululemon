package com.mediaserver.application.usecase.movie;

import com.mediaserver.domain.model.Movie;

public interface GetMovieUseCase {
    Movie getMovie(String id);
}
