package com.mediaserver.infrastructure.rest.mapper;

import com.mediaserver.entity.DownloadTask;
import com.mediaserver.infrastructure.rest.dto.DownloadProgressDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DownloadRestMapper {

    @Mapping(target = "movieId", source = "movie.id")
    @Mapping(target = "movieTitle", source = "movie.title")
    DownloadProgressDto toResponse(DownloadTask downloadTask);
}
