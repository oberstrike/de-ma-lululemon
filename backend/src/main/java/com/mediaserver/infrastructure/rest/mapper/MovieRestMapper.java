package com.mediaserver.infrastructure.rest.mapper;

import com.mediaserver.application.command.CreateMovieCommand;
import com.mediaserver.application.command.UpdateMovieCommand;
import com.mediaserver.entity.Movie;
import com.mediaserver.infrastructure.rest.dto.MovieRequestDto;
import com.mediaserver.infrastructure.rest.dto.MovieResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MovieRestMapper {

    @Mapping(target = "cached", expression = "java(movie.isCached())")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    MovieResponseDto toResponse(Movie movie);

    CreateMovieCommand toCreateCommand(MovieRequestDto dto);

    @Mapping(target = "id", source = "id")
    UpdateMovieCommand toUpdateCommand(String id, MovieRequestDto dto);
}
