package com.mediaserver.application.usecase.stream;

import com.mediaserver.infrastructure.rest.dto.StreamInfoDto;

public interface GetStreamInfoUseCase {
    StreamInfoDto getStreamInfo(String movieId);
}
