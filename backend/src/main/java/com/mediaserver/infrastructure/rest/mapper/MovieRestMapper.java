package com.mediaserver.infrastructure.rest.mapper;

import com.mediaserver.application.command.CreateMovieCommand;
import com.mediaserver.application.command.UpdateMovieCommand;
import com.mediaserver.domain.model.Movie;
import com.mediaserver.infrastructure.rest.dto.MovieRequestDTO;
import com.mediaserver.infrastructure.rest.dto.MovieResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MovieRestMapper {

    @Mapping(target = "cached", expression = "java(movie.isCached())")
    @Mapping(target = "categoryName", ignore = true)
    MovieResponseDTO toResponse(Movie movie);

    CreateMovieCommand toCreateCommand(MovieRequestDTO dto);

    @Mapping(target = "id", source = "id")
    UpdateMovieCommand toUpdateCommand(String id, MovieRequestDTO dto);
}
