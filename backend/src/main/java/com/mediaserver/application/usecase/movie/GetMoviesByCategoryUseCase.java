package com.mediaserver.application.usecase.movie;

import com.mediaserver.domain.model.Movie;
import java.util.List;

public interface GetMoviesByCategoryUseCase {
    List<Movie> getMoviesByCategory(String categoryId);
}
