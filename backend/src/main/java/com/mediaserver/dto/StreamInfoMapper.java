package com.mediaserver.dto;

import com.mediaserver.entity.Movie;
import com.mediaserver.rules.StreamInfoRules;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StreamInfoMapper {
    @Mapping(target = "movieId", source = "id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "fileSize", expression = "java(rules.fileSize(movie))")
    @Mapping(target = "contentType", expression = "java(rules.contentType(movie))")
    @Mapping(target = "streamUrl", expression = "java(rules.streamUrl(movie))")
    @Mapping(target = "supportsRangeRequests", expression = "java(rules.supportsRangeRequests(movie))")
    StreamInfoDto toDto(Movie movie, @Context StreamInfoRules rules);
}
