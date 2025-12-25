package com.mediaserver.application.usecase.movie;

import com.mediaserver.entity.Movie;
import java.util.List;

public interface GetCachedMoviesUseCase {
    List<Movie> getCachedMovies();
}
