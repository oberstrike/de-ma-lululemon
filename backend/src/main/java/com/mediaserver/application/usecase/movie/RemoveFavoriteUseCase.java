package com.mediaserver.application.usecase.movie;

import com.mediaserver.domain.model.Movie;

public interface RemoveFavoriteUseCase {
    Movie removeFavorite(String movieId);
}
