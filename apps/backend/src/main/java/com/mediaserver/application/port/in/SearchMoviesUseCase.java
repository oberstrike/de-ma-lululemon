package com.mediaserver.application.port.in;

import com.mediaserver.domain.model.Movie;

import java.util.List;

/** Use case for searching movies. Defines the input port for movie search operations. */
public interface SearchMoviesUseCase {

    /**
     * Searches movies by title or description.
     *
     * @param query the search query
     * @return list of matching movies
     */
    List<Movie> searchMovies(String query);
}
