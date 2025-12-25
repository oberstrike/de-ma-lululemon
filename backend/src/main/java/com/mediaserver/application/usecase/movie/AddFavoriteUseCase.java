package com.mediaserver.application.usecase.movie;

import com.mediaserver.domain.model.Movie;

public interface AddFavoriteUseCase {
    Movie addFavorite(String movieId);
}
