package com.mediaserver.domain.model;

import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 * Represents a group of movies, typically organized by category or a special grouping like
 * "Favorites" or "Downloaded".
 */
@Value
@Builder
public class MovieGroup {
    /** The display name of the group (category name or special group name). */
    String name;

    /** The category ID if this group represents a category, null for special groups. */
    String categoryId;

    /** Whether this is a special group (Favorites, Downloaded) vs a category. */
    @Builder.Default
    boolean special = false;

    /** The sort order for display purposes. */
    @Builder.Default
    int sortOrder = 0;

    /** The movies in this group. */
    List<Movie> movies;
}
