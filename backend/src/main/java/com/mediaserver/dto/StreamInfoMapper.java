package com.mediaserver.dto;

import com.mediaserver.entity.Movie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StreamInfoMapper {
    @Mapping(target = "movieId", source = "id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "fileSize", expression = "java(movie.getFileSize() != null ? movie.getFileSize() : 0)")
    @Mapping(target = "contentType", expression = "java(movie.getContentType() != null ? movie.getContentType() : \"video/mp4\")")
    @Mapping(target = "streamUrl", expression = "java(\"/api/stream/\" + movie.getId())")
    @Mapping(target = "supportsRangeRequests", expression = "java(true)")
    StreamInfoDto toDto(Movie movie);
}
