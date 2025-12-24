package com.mediaserver.controller;

import com.mediaserver.dto.DownloadProgressDto;
import com.mediaserver.entity.DownloadStatus;
import com.mediaserver.entity.DownloadTask;
import com.mediaserver.entity.Movie;
import com.mediaserver.config.MediaProperties;
import com.mediaserver.config.WebConfig;
import com.mediaserver.exception.GlobalExceptionHandler;
import com.mediaserver.repository.DownloadTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DownloadController.class)
@Import({GlobalExceptionHandler.class, MediaProperties.class, WebConfig.class})
@WithMockUser(username = "admin", roles = "ADMIN")
class DownloadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DownloadTaskRepository taskRepository;

    private DownloadTask testTask;
    private Movie testMovie;

    @BeforeEach
    void setUp() {
        testMovie = Movie.builder()
                .id("movie-1")
                .title("Test Movie")
                .build();

        testTask = DownloadTask.builder()
                .id("task-1")
                .movie(testMovie)
                .status(DownloadStatus.IN_PROGRESS)
                .bytesDownloaded(500L * 1024 * 1024)
                .totalBytes(1024L * 1024 * 1024)
                .progress(50)
                .build();
    }

    @Test
    void getActiveDownloads_shouldReturnDownloadList() throws Exception {
        when(taskRepository.findActiveDownloads()).thenReturn(List.of(testTask));

        mockMvc.perform(get("/api/downloads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].movieId").value("movie-1"))
                .andExpect(jsonPath("$[0].movieTitle").value("Test Movie"))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$[0].progress").value(50));
    }

    @Test
    void getActiveDownloads_shouldReturnEmptyList_whenNoActiveDownloads() throws Exception {
        when(taskRepository.findActiveDownloads()).thenReturn(List.of());

        mockMvc.perform(get("/api/downloads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getDownloadProgress_shouldReturnProgress_whenExists() throws Exception {
        when(taskRepository.findByMovieId("movie-1")).thenReturn(Optional.of(testTask));

        mockMvc.perform(get("/api/downloads/movie-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movieId").value("movie-1"))
                .andExpect(jsonPath("$.progress").value(50))
                .andExpect(jsonPath("$.bytesDownloaded").value(500L * 1024 * 1024))
                .andExpect(jsonPath("$.totalBytes").value(1024L * 1024 * 1024));
    }

    @Test
    void getDownloadProgress_shouldReturn404_whenNotFound() throws Exception {
        when(taskRepository.findByMovieId("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/downloads/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getDownloadProgress_shouldHandleNullValues() throws Exception {
        testTask.setBytesDownloaded(null);
        testTask.setTotalBytes(null);
        testTask.setProgress(null);
        when(taskRepository.findByMovieId("movie-1")).thenReturn(Optional.of(testTask));

        mockMvc.perform(get("/api/downloads/movie-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bytesDownloaded").value(0))
                .andExpect(jsonPath("$.totalBytes").value(0))
                .andExpect(jsonPath("$.progress").value(0));
    }
}
