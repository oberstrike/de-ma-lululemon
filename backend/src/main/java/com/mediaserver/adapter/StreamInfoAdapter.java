package com.mediaserver.adapter;

import com.mediaserver.dto.StreamInfoDto;
import com.mediaserver.entity.Movie;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

@Component
public class StreamInfoAdapter {
    private final ConversionService conversionService;

    public StreamInfoAdapter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public StreamInfoDto toDto(Movie movie) {
        return conversionService.convert(movie, StreamInfoDto.class);
    }
}
