package com.mediaserver.dto;

import com.mediaserver.entity.Movie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MovieMapper {

    @Mapping(target = "cached", expression = "java(movie.isCached())")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    MovieDto toDto(Movie movie);
}
