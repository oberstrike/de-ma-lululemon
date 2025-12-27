package com.mediaserver.application.usecase.movie;

import com.mediaserver.domain.model.Movie;

import java.util.List;

public interface SearchMoviesUseCase {
    List<Movie> searchMovies(String query);
}
