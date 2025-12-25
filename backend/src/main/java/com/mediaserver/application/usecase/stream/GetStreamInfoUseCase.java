package com.mediaserver.application.usecase.stream;

import lombok.Builder;
import lombok.Value;

public interface GetStreamInfoUseCase {
    StreamInfo getStreamInfo(String movieId);

    /** Stream information for a movie. */
    @Value
    @Builder
    class StreamInfo {
        String movieId;
        String title;
        long fileSize;
        String contentType;
        String streamUrl;
        boolean supportsRangeRequests;
    }
}
