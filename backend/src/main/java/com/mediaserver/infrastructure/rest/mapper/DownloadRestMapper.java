package com.mediaserver.infrastructure.rest.mapper;

import com.mediaserver.domain.model.DownloadTask;
import com.mediaserver.infrastructure.rest.dto.DownloadProgressDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DownloadRestMapper {

    @Mapping(target = "movieTitle", ignore = true)
    DownloadProgressDTO toResponse(DownloadTask downloadTask);
}
