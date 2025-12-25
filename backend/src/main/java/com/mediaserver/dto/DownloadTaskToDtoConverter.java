package com.mediaserver.dto;

import com.mediaserver.entity.DownloadTask;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class DownloadTaskToDtoConverter implements Converter<DownloadTask, DownloadProgressDto> {
    private final DownloadTaskMapper downloadTaskMapper;

    public DownloadTaskToDtoConverter(DownloadTaskMapper downloadTaskMapper) {
        this.downloadTaskMapper = downloadTaskMapper;
    }

    @Override
    public DownloadProgressDto convert(DownloadTask source) {
        return downloadTaskMapper.toDto(source);
    }
}
