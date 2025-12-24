package com.mediaserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediaserver.config.MediaProperties;
import com.mediaserver.config.WebConfig;
import com.mediaserver.dto.CacheStatsDto;
import com.mediaserver.dto.MovieCreateRequest;
import com.mediaserver.dto.MovieDto;
import com.mediaserver.entity.MovieStatus;
import com.mediaserver.exception.GlobalExceptionHandler;
import com.mediaserver.exception.MovieNotFoundException;
import com.mediaserver.service.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovieController.class)
@Import({GlobalExceptionHandler.class, MediaProperties.class, WebConfig.class})
@WithMockUser(username = "admin", roles = "ADMIN")
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MovieService movieService;

    private MovieDto testMovieDto;

    @BeforeEach
    void setUp() {
        testMovieDto = MovieDto.builder()
                .id("movie-1")
                .title("Test Movie")
                .description("A test movie")
                .year(2024)
                .duration("2h 30m")
                .status(MovieStatus.PENDING)
                .cached(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllMovies_shouldReturnMovieList() throws Exception {
        when(movieService.getAllMovies()).thenReturn(List.of(testMovieDto));

        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("movie-1"))
                .andExpect(jsonPath("$[0].title").value("Test Movie"));
    }

    @Test
    void getAllMovies_withSearch_shouldCallSearchMovies() throws Exception {
        when(movieService.searchMovies("test")).thenReturn(List.of(testMovieDto));

        mockMvc.perform(get("/api/movies").param("search", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Movie"));

        verify(movieService).searchMovies("test");
    }

    @Test
    void getAllMovies_withReadyOnly_shouldCallGetReadyMovies() throws Exception {
        testMovieDto.setStatus(MovieStatus.READY);
        testMovieDto.setCached(true);
        when(movieService.getReadyMovies()).thenReturn(List.of(testMovieDto));

        mockMvc.perform(get("/api/movies").param("readyOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("READY"));

        verify(movieService).getReadyMovies();
    }

    @Test
    void getMovie_shouldReturnMovie_whenExists() throws Exception {
        when(movieService.getMovie("movie-1")).thenReturn(testMovieDto);

        mockMvc.perform(get("/api/movies/movie-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("movie-1"))
                .andExpect(jsonPath("$.title").value("Test Movie"));
    }

    @Test
    void getMovie_shouldReturn404_whenNotFound() throws Exception {
        when(movieService.getMovie("nonexistent"))
                .thenThrow(new MovieNotFoundException("nonexistent"));

        mockMvc.perform(get("/api/movies/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createMovie_shouldReturnCreatedMovie() throws Exception {
        MovieCreateRequest request = MovieCreateRequest.builder()
                .title("New Movie")
                .megaUrl("https://mega.nz/file/test")
                .build();

        when(movieService.createMovie(any(MovieCreateRequest.class))).thenReturn(testMovieDto);

        mockMvc.perform(post("/api/movies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("movie-1"));
    }

    @Test
    void createMovie_shouldReturn400_whenTitleMissing() throws Exception {
        MovieCreateRequest request = MovieCreateRequest.builder()
                .megaUrl("https://mega.nz/file/test")
                .build();

        mockMvc.perform(post("/api/movies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMovie_shouldReturn400_whenMegaUrlMissing() throws Exception {
        MovieCreateRequest request = MovieCreateRequest.builder()
                .title("New Movie")
                .build();

        mockMvc.perform(post("/api/movies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMovie_shouldReturnUpdatedMovie() throws Exception {
        MovieCreateRequest request = MovieCreateRequest.builder()
                .title("Updated Movie")
                .megaUrl("https://mega.nz/file/test")
                .build();

        testMovieDto.setTitle("Updated Movie");
        when(movieService.updateMovie(eq("movie-1"), any(MovieCreateRequest.class)))
                .thenReturn(testMovieDto);

        mockMvc.perform(put("/api/movies/movie-1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Movie"));
    }

    @Test
    void deleteMovie_shouldReturn204() throws Exception {
        doNothing().when(movieService).deleteMovie("movie-1");

        mockMvc.perform(delete("/api/movies/movie-1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(movieService).deleteMovie("movie-1");
    }

    @Test
    void startDownload_shouldReturn202() throws Exception {
        doNothing().when(movieService).startDownload("movie-1");

        mockMvc.perform(post("/api/movies/movie-1/download")
                        .with(csrf()))
                .andExpect(status().isAccepted());

        verify(movieService).startDownload("movie-1");
    }

    @Test
    void getCacheStats_shouldReturnStats() throws Exception {
        CacheStatsDto stats = CacheStatsDto.builder()
                .totalSizeBytes(1024L * 1024 * 1024)
                .maxSizeBytes(100L * 1024 * 1024 * 1024)
                .usagePercent(1)
                .movieCount(5)
                .build();

        when(movieService.getCacheStats()).thenReturn(stats);

        mockMvc.perform(get("/api/movies/cache/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movieCount").value(5))
                .andExpect(jsonPath("$.usagePercent").value(1));
    }
}
