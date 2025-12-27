package com.mediaserver.infrastructure.rest.mapper;

import com.mediaserver.application.command.CreateMovieCommand;
import com.mediaserver.application.command.UpdateMovieCommand;
import com.mediaserver.domain.model.Category;
import com.mediaserver.domain.model.Movie;
import com.mediaserver.domain.model.MovieGroup;
import com.mediaserver.infrastructure.rest.dto.MovieGroupResponseDTO;
import com.mediaserver.infrastructure.rest.dto.MovieRequestDTO;
import com.mediaserver.infrastructure.rest.dto.MovieResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface MovieRestMapper {

    @Mapping(target = "cached", expression = "java(movie.isCached())")
    @Mapping(target = "categoryName", ignore = true)
    MovieResponseDTO toResponse(Movie movie);

    /**
     * Convert a Movie to MovieResponseDTO with category name lookup.
     *
     * @param movie the movie domain object
     * @param categoriesById map of category ID to Category for name lookup
     * @return the response DTO with categoryName populated
     */
    default MovieResponseDTO toResponseWithCategory(
            Movie movie, Map<String, Category> categoriesById) {
        MovieResponseDTO dto = toResponse(movie);
        if (movie.getCategoryId() != null && categoriesById != null) {
            Category category = categoriesById.get(movie.getCategoryId());
            if (category != null) {
                dto.setCategoryName(category.getName());
            }
        }
        return dto;
    }

    /**
     * Convert a list of Movies to MovieResponseDTOs with category name lookup.
     *
     * @param movies the movie domain objects
     * @param categoriesById map of category ID to Category for name lookup
     * @return list of response DTOs with categoryName populated
     */
    default List<MovieResponseDTO> toResponseListWithCategories(
            List<Movie> movies, Map<String, Category> categoriesById) {
        return movies.stream().map(m -> toResponseWithCategory(m, categoriesById)).toList();
    }

    /**
     * Convert a MovieGroup to MovieGroupResponseDTO.
     *
     * @param group the movie group domain object
     * @param categoriesById map of category ID to Category for name lookup
     * @return the response DTO
     */
    default MovieGroupResponseDTO toGroupResponse(
            MovieGroup group, Map<String, Category> categoriesById) {
        return MovieGroupResponseDTO.builder()
                .name(group.getName())
                .categoryId(group.getCategoryId())
                .special(group.isSpecial())
                .sortOrder(group.getSortOrder())
                .movies(toResponseListWithCategories(group.getMovies(), categoriesById))
                .build();
    }

    /**
     * Convert a list of MovieGroups to MovieGroupResponseDTOs.
     *
     * @param groups the movie group domain objects
     * @param categoriesById map of category ID to Category for name lookup
     * @return list of response DTOs
     */
    default List<MovieGroupResponseDTO> toGroupResponseList(
            List<MovieGroup> groups, Map<String, Category> categoriesById) {
        return groups.stream().map(g -> toGroupResponse(g, categoriesById)).toList();
    }

    CreateMovieCommand toCreateCommand(MovieRequestDTO dto);

    @Mapping(target = "id", source = "id")
    UpdateMovieCommand toUpdateCommand(String id, MovieRequestDTO dto);
}
