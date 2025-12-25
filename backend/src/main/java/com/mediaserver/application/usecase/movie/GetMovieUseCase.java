package com.mediaserver.application.usecase.movie;

import com.mediaserver.entity.Movie;

public interface GetMovieUseCase {
    Movie getMovie(String id);
}
