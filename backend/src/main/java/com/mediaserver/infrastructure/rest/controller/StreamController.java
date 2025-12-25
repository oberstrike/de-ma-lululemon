package com.mediaserver.infrastructure.rest.controller;

import com.mediaserver.application.usecase.stream.GetStreamInfoUseCase;
import com.mediaserver.infrastructure.rest.dto.StreamInfoDto;
import com.mediaserver.service.VideoStreamingService;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/stream")
@RequiredArgsConstructor
@Slf4j
public class StreamController {

    private final VideoStreamingService streamingService;
    private final GetStreamInfoUseCase getStreamInfoUseCase;

    @GetMapping("/{movieId}")
    public ResponseEntity<StreamingResponseBody> streamVideo(
            @PathVariable String movieId,
            @RequestHeader(value = "Range", required = false) String rangeHeader) {

        try {
            var response = streamingService.streamVideo(movieId, rangeHeader);

            var headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(response.getContentType()));
            headers.setContentLength(response.getContentLength());
            headers.set("Accept-Ranges", "bytes");

            var streamingBody = createStreamingBody(response);

            if (response.isPartial()) {
                headers.set(
                        "Content-Range",
                        String.format(
                                "bytes %d-%d/%d",
                                response.getRangeStart(),
                                response.getRangeEnd(),
                                response.getFileSize()));

                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .headers(headers)
                        .body(streamingBody);
            }

            return ResponseEntity.ok().headers(headers).body(streamingBody);

        } catch (IOException e) {
            log.error("Error streaming video: {}", movieId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private StreamingResponseBody createStreamingBody(
            VideoStreamingService.StreamingResponse response) {
        return outputStream -> {
            try (InputStream is = response.getInputStreamSupplier().get()) {
                is.transferTo(outputStream);
            }
        };
    }

    @GetMapping("/{movieId}/info")
    public StreamInfoDto getStreamInfo(@PathVariable String movieId) {
        return getStreamInfoUseCase.getStreamInfo(movieId);
    }
}
