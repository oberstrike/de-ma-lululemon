package com.mediaserver.infrastructure.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO representing a group of movies for the REST API. Groups can be categories or special groups
 * like "Favorites" or "Downloaded".
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieGroupResponseDTO {
    /** The display name of the group. */
    private String name;

    /** The category ID if this is a category group, null for special groups. */
    private String categoryId;

    /** Whether this is a special group (Favorites, Downloaded, Other). */
    private boolean special;

    /** Sort order for display. */
    private int sortOrder;

    /** The movies in this group. */
    private List<MovieResponseDTO> movies;
}
