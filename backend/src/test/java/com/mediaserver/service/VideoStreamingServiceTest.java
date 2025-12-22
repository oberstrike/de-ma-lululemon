package com.mediaserver.service;

import com.mediaserver.config.MediaProperties;
import com.mediaserver.entity.Movie;
import com.mediaserver.entity.MovieStatus;
import com.mediaserver.exception.MovieNotFoundException;
import com.mediaserver.exception.VideoNotReadyException;
import com.mediaserver.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoStreamingServiceTest {

    @Mock
    private MediaProperties properties;

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private VideoStreamingService videoStreamingService;

    @TempDir
    Path tempDir;

    private Movie testMovie;
    private Path testVideoPath;

    @BeforeEach
    void setUp() throws IOException {
        // Create a test video file
        testVideoPath = tempDir.resolve("test-video.mp4");
        byte[] testContent = new byte[1024 * 1024]; // 1MB of test data
        Files.write(testVideoPath, testContent);

        testMovie = Movie.builder()
                .id("movie-1")
                .title("Test Movie")
                .status(MovieStatus.READY)
                .localPath(testVideoPath.toString())
                .fileSize(1024L * 1024)
                .contentType("video/mp4")
                .build();

        MediaProperties.Streaming streaming = new MediaProperties.Streaming();
        streaming.setChunkSize(1048576);
        when(properties.getStreaming()).thenReturn(streaming);
    }

    @Test
    void streamVideo_shouldThrowException_whenMovieNotFound() {
        when(movieRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> videoStreamingService.streamVideo("nonexistent", null))
                .isInstanceOf(MovieNotFoundException.class);
    }

    @Test
    void streamVideo_shouldThrowException_whenVideoNotReady() {
        testMovie.setStatus(MovieStatus.PENDING);
        testMovie.setLocalPath(null);
        when(movieRepository.findById("movie-1")).thenReturn(Optional.of(testMovie));

        assertThatThrownBy(() -> videoStreamingService.streamVideo("movie-1", null))
                .isInstanceOf(VideoNotReadyException.class)
                .hasMessageContaining("not yet downloaded");
    }

    @Test
    void streamVideo_shouldReturnFullContent_whenNoRangeHeader() throws IOException {
        when(movieRepository.findById("movie-1")).thenReturn(Optional.of(testMovie));

        VideoStreamingService.StreamingResponse response =
                videoStreamingService.streamVideo("movie-1", null);

        assertThat(response.isPartial()).isFalse();
        assertThat(response.getContentType()).isEqualTo("video/mp4");
        assertThat(response.getFileSize()).isEqualTo(1024L * 1024);
        assertThat(response.getRangeStart()).isEqualTo(0);
    }

    @Test
    void streamVideo_shouldReturnPartialContent_withRangeHeader() throws IOException {
        when(movieRepository.findById("movie-1")).thenReturn(Optional.of(testMovie));

        VideoStreamingService.StreamingResponse response =
                videoStreamingService.streamVideo("movie-1", "bytes=0-999");

        assertThat(response.isPartial()).isTrue();
        assertThat(response.getRangeStart()).isEqualTo(0);
        assertThat(response.getRangeEnd()).isEqualTo(999);
        assertThat(response.getContentLength()).isEqualTo(1000);
    }

    @Test
    void streamVideo_shouldHandleOpenEndedRange() throws IOException {
        when(movieRepository.findById("movie-1")).thenReturn(Optional.of(testMovie));

        VideoStreamingService.StreamingResponse response =
                videoStreamingService.streamVideo("movie-1", "bytes=500-");

        assertThat(response.isPartial()).isTrue();
        assertThat(response.getRangeStart()).isEqualTo(500);
    }

    @Test
    void streamVideo_shouldLimitChunkSize() throws IOException {
        // Set a small chunk size for testing
        MediaProperties.Streaming streaming = new MediaProperties.Streaming();
        streaming.setChunkSize(100);
        when(properties.getStreaming()).thenReturn(streaming);
        when(movieRepository.findById("movie-1")).thenReturn(Optional.of(testMovie));

        VideoStreamingService.StreamingResponse response =
                videoStreamingService.streamVideo("movie-1", "bytes=0-999999");

        // Should be limited to chunk size
        assertThat(response.getContentLength()).isEqualTo(100);
        assertThat(response.getRangeEnd()).isEqualTo(99);
    }
}
