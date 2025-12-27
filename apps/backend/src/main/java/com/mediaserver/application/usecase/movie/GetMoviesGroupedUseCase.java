package com.mediaserver.application.usecase.movie;

import com.mediaserver.domain.model.MovieGroup;

import java.util.List;

/**
 * Use case for getting movies grouped by category with special groups for favorites and cached
 * movies.
 */
public interface GetMoviesGroupedUseCase {

    /**
     * Get all movies grouped by category and special groups.
     *
     * <p>Returns groups in the following order: 1. My Favorites (if any favorites exist) 2.
     * Downloaded on Server (if any cached movies exist) 3. Category groups (ordered by category
     * sort order) 4. Other Movies (uncategorized movies)
     *
     * @return list of movie groups
     */
    List<MovieGroup> getMoviesGrouped();

    /**
     * Get movies grouped by category with optional search filter.
     *
     * @param search optional search query to filter movies by title
     * @return list of movie groups containing matching movies
     */
    List<MovieGroup> getMoviesGrouped(String search);
}
