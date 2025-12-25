package com.mediaserver.dto;

import com.mediaserver.entity.DownloadTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DownloadTaskMapper {
    @Mapping(target = "movieId", source = "movie.id")
    @Mapping(target = "movieTitle", source = "movie.title")
    @Mapping(target = "bytesDownloaded", expression = "java(task.getBytesDownloaded() != null ? task.getBytesDownloaded() : 0)")
    @Mapping(target = "totalBytes", expression = "java(task.getTotalBytes() != null ? task.getTotalBytes() : 0)")
    @Mapping(target = "progress", expression = "java(task.getProgress() != null ? task.getProgress() : 0)")
    DownloadProgressDto toDto(DownloadTask task);
}
