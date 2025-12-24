package com.mediaserver.controller;

import com.mediaserver.dto.StreamInfoDto;
import com.mediaserver.entity.Movie;
import com.mediaserver.entity.MovieStatus;
import com.mediaserver.config.MediaProperties;
import com.mediaserver.config.WebConfig;
import com.mediaserver.exception.GlobalExceptionHandler;
import com.mediaserver.exception.MovieNotFoundException;
import com.mediaserver.exception.VideoNotReadyException;
import com.mediaserver.repository.MovieRepository;
import com.mediaserver.service.VideoStreamingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StreamController.class)
@Import({GlobalExceptionHandler.class, MediaProperties.class, WebConfig.class})
@WithMockUser(username = "admin", roles = "ADMIN")
class StreamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VideoStreamingService streamingService;

    @MockitoBean
    private MovieRepository movieRepository;

    private Movie testMovie;

    @BeforeEach
    void setUp() {
        testMovie = Movie.builder()
                .id("movie-1")
                .title("Test Movie")
                .status(MovieStatus.READY)
                .localPath("/path/to/video.mp4")
                .fileSize(1024L * 1024 * 100)
                .contentType("video/mp4")
                .build();
    }

    @Test
    void streamVideo_shouldReturnFullContent_whenNoRangeHeader() throws Exception {
        byte[] testContent = "test video content".getBytes();

        VideoStreamingService.StreamingResponse response = VideoStreamingService.StreamingResponse.builder()
                .inputStreamSupplier(() -> new ByteArrayInputStream(testContent))
                .contentType("video/mp4")
                .contentLength(testContent.length)
                .fileSize(testContent.length)
                .rangeStart(0)
                .rangeEnd(testContent.length - 1)
                .isPartial(false)
                .build();

        when(streamingService.streamVideo(eq("movie-1"), any())).thenReturn(response);

        mockMvc.perform(get("/api/stream/movie-1"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "video/mp4"))
                .andExpect(header().string("Accept-Ranges", "bytes"));
    }

    @Test
    void streamVideo_shouldReturnPartialContent_withRangeHeader() throws Exception {
        byte[] testContent = "partial content".getBytes();

        VideoStreamingService.StreamingResponse response = VideoStreamingService.StreamingResponse.builder()
                .inputStreamSupplier(() -> new ByteArrayInputStream(testContent))
                .contentType("video/mp4")
                .contentLength(testContent.length)
                .fileSize(1000)
                .rangeStart(0)
                .rangeEnd(testContent.length - 1)
                .isPartial(true)
                .build();

        when(streamingService.streamVideo(eq("movie-1"), eq("bytes=0-99"))).thenReturn(response);

        mockMvc.perform(get("/api/stream/movie-1")
                        .header("Range", "bytes=0-99"))
                .andExpect(status().isPartialContent())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "video/mp4"))
                .andExpect(header().string("Accept-Ranges", "bytes"))
                .andExpect(header().exists("Content-Range"));
    }

    @Test
    void streamVideo_shouldReturn404_whenMovieNotFound() throws Exception {
        when(streamingService.streamVideo(eq("nonexistent"), any()))
                .thenThrow(new MovieNotFoundException("nonexistent"));

        mockMvc.perform(get("/api/stream/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void streamVideo_shouldReturn409_whenVideoNotReady() throws Exception {
        when(streamingService.streamVideo(eq("movie-1"), any()))
                .thenThrow(new VideoNotReadyException("Video is not yet downloaded"));

        mockMvc.perform(get("/api/stream/movie-1"))
                .andExpect(status().isConflict());
    }

    @Test
    void streamVideo_shouldReturn500_onIOException() throws Exception {
        when(streamingService.streamVideo(eq("movie-1"), any()))
                .thenThrow(new IOException("Read error"));

        mockMvc.perform(get("/api/stream/movie-1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getStreamInfo_shouldReturnStreamInfo() throws Exception {
        when(movieRepository.findById("movie-1")).thenReturn(Optional.of(testMovie));

        mockMvc.perform(get("/api/stream/movie-1/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movieId").value("movie-1"))
                .andExpect(jsonPath("$.title").value("Test Movie"))
                .andExpect(jsonPath("$.fileSize").value(1024L * 1024 * 100))
                .andExpect(jsonPath("$.contentType").value("video/mp4"))
                .andExpect(jsonPath("$.streamUrl").value("/api/stream/movie-1"))
                .andExpect(jsonPath("$.supportsRangeRequests").value(true));
    }

    @Test
    void getStreamInfo_shouldReturn404_whenMovieNotFound() throws Exception {
        when(movieRepository.findById("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/stream/nonexistent/info"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getStreamInfo_shouldHandleNullFileSize() throws Exception {
        testMovie.setFileSize(null);
        testMovie.setContentType(null);
        when(movieRepository.findById("movie-1")).thenReturn(Optional.of(testMovie));

        mockMvc.perform(get("/api/stream/movie-1/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileSize").value(0))
                .andExpect(jsonPath("$.contentType").value("video/mp4"));
    }

    @Test
    void streamVideo_shouldSetCorrectContentRangeHeader() throws Exception {
        byte[] testContent = new byte[100];

        VideoStreamingService.StreamingResponse response = VideoStreamingService.StreamingResponse.builder()
                .inputStreamSupplier(() -> new ByteArrayInputStream(testContent))
                .contentType("video/mp4")
                .contentLength(100)
                .fileSize(1000)
                .rangeStart(500)
                .rangeEnd(599)
                .isPartial(true)
                .build();

        when(streamingService.streamVideo(eq("movie-1"), eq("bytes=500-599"))).thenReturn(response);

        mockMvc.perform(get("/api/stream/movie-1")
                        .header("Range", "bytes=500-599"))
                .andExpect(status().isPartialContent())
                .andExpect(header().string("Content-Range", "bytes 500-599/1000"));
    }
}
