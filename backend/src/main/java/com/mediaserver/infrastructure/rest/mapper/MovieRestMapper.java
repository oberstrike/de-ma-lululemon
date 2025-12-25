package com.mediaserver.infrastructure.rest.mapper;

import com.mediaserver.application.command.CreateMovieCommand;
import com.mediaserver.application.command.UpdateMovieCommand;
import com.mediaserver.domain.model.Movie;
import com.mediaserver.infrastructure.rest.dto.MovieRequestDto;
import com.mediaserver.infrastructure.rest.dto.MovieResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MovieRestMapper {

    @Mapping(target = "cached", expression = "java(movie.isCached())")
    @Mapping(target = "categoryName", ignore = true)
    MovieResponseDto toResponse(Movie movie);

    CreateMovieCommand toCreateCommand(MovieRequestDto dto);

    @Mapping(target = "id", source = "id")
    UpdateMovieCommand toUpdateCommand(String id, MovieRequestDto dto);
}
