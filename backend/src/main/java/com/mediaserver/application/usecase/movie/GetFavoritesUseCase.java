package com.mediaserver.application.usecase.movie;

import com.mediaserver.domain.model.Movie;
import java.util.List;

public interface GetFavoritesUseCase {
    List<Movie> getFavorites();
}
