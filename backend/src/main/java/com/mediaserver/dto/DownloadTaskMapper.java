package com.mediaserver.dto;

import com.mediaserver.entity.DownloadTask;
import com.mediaserver.rules.DownloadTaskRules;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DownloadTaskMapper {
    @Mapping(target = "movieId", expression = "java(rules.movieId(task))")
    @Mapping(target = "movieTitle", expression = "java(rules.movieTitle(task))")
    @Mapping(target = "bytesDownloaded", expression = "java(rules.bytesDownloaded(task))")
    @Mapping(target = "totalBytes", expression = "java(rules.totalBytes(task))")
    @Mapping(target = "progress", expression = "java(rules.progress(task))")
    DownloadProgressDto toDto(DownloadTask task, @Context DownloadTaskRules rules);
}
