package com.mediaserver.dto;

import com.mediaserver.entity.Movie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface MovieMapper {

    @Mapping(target = "cached", expression = "java(movie.isCached())")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "thumbnailUrl", source = "movie", qualifiedByName = "toThumbnailUrl")
    MovieDto toDto(Movie movie);

    @Named("toThumbnailUrl")
    default String toThumbnailUrl(Movie movie) {
        // If thumbnailUrl starts with "/" (Mega path), convert to API endpoint
        if (movie.getThumbnailUrl() != null && movie.getThumbnailUrl().startsWith("/")) {
            return "/api/thumbnails/" + movie.getId();
        }
        return movie.getThumbnailUrl();
    }
}
