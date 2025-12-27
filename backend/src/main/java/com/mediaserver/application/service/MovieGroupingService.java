package com.mediaserver.application.service;

import com.mediaserver.application.port.out.CategoryPort;
import com.mediaserver.application.port.out.CurrentUserProvider;
import com.mediaserver.application.port.out.MoviePort;
import com.mediaserver.application.usecase.movie.GetMoviesGroupedUseCase;
import com.mediaserver.domain.model.Category;
import com.mediaserver.domain.model.Movie;
import com.mediaserver.domain.model.MovieGroup;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for grouping movies by category and creating special groups for favorites and
 * cached movies.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovieGroupingService implements GetMoviesGroupedUseCase {

    private static final String FAVORITES_GROUP = "My Favorites";
    private static final String CACHED_GROUP = "Downloaded on Server";
    private static final String UNCATEGORIZED_GROUP = "Other Movies";

    private final MoviePort moviePort;
    private final CategoryPort categoryPort;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public List<MovieGroup> getMoviesGrouped() {
        return getMoviesGrouped(null);
    }

    @Override
    public List<MovieGroup> getMoviesGrouped(String search) {
        String userId = currentUserProvider.getCurrentUserId();
        List<Movie> allMovies =
                (search != null && !search.isBlank())
                        ? moviePort.search(search)
                        : moviePort.findAll();
        allMovies = moviePort.applyFavoriteStatus(allMovies, userId);

        Map<String, Category> categoriesById =
                categoryPort.findAllOrderedBySortOrder().stream()
                        .collect(
                                Collectors.toMap(
                                        Category::getId, c -> c, (a, b) -> a, LinkedHashMap::new));

        List<MovieGroup> groups = new ArrayList<>();
        int sortOrder = 0;

        List<Movie> favorites = allMovies.stream().filter(Movie::isFavorite).toList();
        if (!favorites.isEmpty()) {
            groups.add(
                    MovieGroup.builder()
                            .name(FAVORITES_GROUP)
                            .special(true)
                            .sortOrder(sortOrder++)
                            .movies(favorites)
                            .build());
        }

        List<Movie> cached = allMovies.stream().filter(Movie::isCached).toList();
        if (!cached.isEmpty()) {
            groups.add(
                    MovieGroup.builder()
                            .name(CACHED_GROUP)
                            .special(true)
                            .sortOrder(sortOrder++)
                            .movies(cached)
                            .build());
        }

        Map<String, List<Movie>> moviesByCategory =
                allMovies.stream()
                        .filter(m -> m.getCategoryId() != null)
                        .collect(Collectors.groupingBy(Movie::getCategoryId));

        for (Category category : categoriesById.values()) {
            List<Movie> categoryMovies = moviesByCategory.get(category.getId());
            if (categoryMovies != null && !categoryMovies.isEmpty()) {
                groups.add(
                        MovieGroup.builder()
                                .name(category.getName())
                                .categoryId(category.getId())
                                .special(false)
                                .sortOrder(sortOrder++)
                                .movies(categoryMovies)
                                .build());
            }
        }

        List<Movie> uncategorized =
                allMovies.stream().filter(m -> m.getCategoryId() == null).toList();
        if (!uncategorized.isEmpty()) {
            groups.add(
                    MovieGroup.builder()
                            .name(UNCATEGORIZED_GROUP)
                            .special(true)
                            .sortOrder(sortOrder)
                            .movies(uncategorized)
                            .build());
        }

        return groups;
    }
}
