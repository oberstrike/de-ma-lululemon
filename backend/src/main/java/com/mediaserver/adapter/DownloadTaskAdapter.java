package com.mediaserver.adapter;

import com.mediaserver.dto.DownloadProgressDto;
import com.mediaserver.entity.DownloadTask;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

@Component
public class DownloadTaskAdapter {
    private final ConversionService conversionService;

    public DownloadTaskAdapter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public DownloadProgressDto toDto(DownloadTask task) {
        return conversionService.convert(task, DownloadProgressDto.class);
    }
}
