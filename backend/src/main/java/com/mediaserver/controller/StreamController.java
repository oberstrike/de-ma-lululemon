package com.mediaserver.controller;

import com.mediaserver.dto.StreamInfoDto;
import com.mediaserver.entity.Movie;
import com.mediaserver.exception.MovieNotFoundException;
import com.mediaserver.repository.MovieRepository;
import com.mediaserver.service.VideoStreamingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/stream")
@RequiredArgsConstructor
@Slf4j
public class StreamController {

    private final VideoStreamingService streamingService;
    private final MovieRepository movieRepository;

    @GetMapping("/{movieId}")
    public ResponseEntity<StreamingResponseBody> streamVideo(
            @PathVariable String movieId,
            @RequestHeader(value = "Range", required = false) String rangeHeader) {

        try {
            VideoStreamingService.StreamingResponse response =
                    streamingService.streamVideo(movieId, rangeHeader);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(response.getContentType()));
            headers.setContentLength(response.getContentLength());
            headers.set("Accept-Ranges", "bytes");

            if (response.isPartial()) {
                headers.set("Content-Range", String.format("bytes %d-%d/%d",
                        response.getRangeStart(),
                        response.getRangeEnd(),
                        response.getFileSize()));

                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .headers(headers)
                        .body(outputStream -> {
                            try (InputStream is = response.getInputStreamSupplier().get()) {
                                is.transferTo(outputStream);
                            }
                        });
            }

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream -> {
                        try (InputStream is = response.getInputStreamSupplier().get()) {
                            is.transferTo(outputStream);
                        }
                    });

        } catch (IOException e) {
            log.error("Error streaming video: {}", movieId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{movieId}/info")
    public ResponseEntity<StreamInfoDto> getStreamInfo(@PathVariable String movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException(movieId));

        StreamInfoDto info = StreamInfoDto.builder()
                .movieId(movie.getId())
                .title(movie.getTitle())
                .fileSize(movie.getFileSize() != null ? movie.getFileSize() : 0)
                .contentType(movie.getContentType() != null ? movie.getContentType() : "video/mp4")
                .streamUrl("/api/stream/" + movie.getId())
                .supportsRangeRequests(true)
                .build();

        return ResponseEntity.ok(info);
    }
}
