package com.mediaserver.application.port.in;

import java.io.InputStream;
import java.util.function.Supplier;
import lombok.Builder;
import lombok.Value;

/**
 * Use case for streaming video content.
 * Defines the input port for video streaming operations.
 */
public interface StreamVideoUseCase {

    /**
     * Streams a video with support for range requests.
     * @param movieId the movie ID
     * @param rangeHeader the HTTP Range header (optional)
     * @return streaming response with video stream
     * @throws com.mediaserver.exception.MovieNotFoundException if movie not found
     * @throws com.mediaserver.exception.VideoNotReadyException if video is not cached
     */
    StreamingResponse streamVideo(String movieId, String rangeHeader);

    /**
     * Streaming response containing video stream and metadata.
     */
    @Value
    @Builder
    class StreamingResponse {
        Supplier<InputStream> inputStreamSupplier;
        String contentType;
        long contentLength;
        long fileSize;
        long rangeStart;
        long rangeEnd;
        boolean isPartial;
    }
}
