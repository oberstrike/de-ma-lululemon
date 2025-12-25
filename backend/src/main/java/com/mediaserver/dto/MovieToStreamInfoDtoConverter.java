package com.mediaserver.dto;

import com.mediaserver.entity.Movie;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class MovieToStreamInfoDtoConverter implements Converter<Movie, StreamInfoDto> {
    private final StreamInfoMapper streamInfoMapper;

    public MovieToStreamInfoDtoConverter(StreamInfoMapper streamInfoMapper) {
        this.streamInfoMapper = streamInfoMapper;
    }

    @Override
    public StreamInfoDto convert(Movie source) {
        return streamInfoMapper.toDto(source);
    }
}
