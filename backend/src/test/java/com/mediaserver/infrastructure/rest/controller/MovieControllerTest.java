package com.mediaserver.infrastructure.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediaserver.application.command.CreateMovieCommand;
import com.mediaserver.application.command.UpdateMovieCommand;
import com.mediaserver.application.usecase.category.GetAllCategoriesUseCase;
import com.mediaserver.application.usecase.movie.*;
import com.mediaserver.config.MediaProperties;
import com.mediaserver.config.WebConfig;
import com.mediaserver.domain.model.Movie;
import com.mediaserver.domain.model.MovieStatus;
import com.mediaserver.exception.GlobalExceptionHandler;
import com.mediaserver.exception.MovieNotFoundException;
import com.mediaserver.infrastructure.rest.dto.MovieRequestDTO;
import com.mediaserver.infrastructure.rest.dto.MovieResponseDTO;
import com.mediaserver.infrastructure.rest.mapper.MovieRestMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Unit tests for MovieController in Clean Architecture. Tests the REST adapter layer that uses
 * application services (use cases).
 */
@WebMvcTest(MovieController.class)
@Import({GlobalExceptionHandler.class, MediaProperties.class, WebConfig.class})
@WithMockUser(username = "admin", roles = "ADMIN")
class MovieControllerTest {

    @Autowired private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean private GetAllMoviesUseCase getAllMoviesUseCase;

    @MockitoBean private GetMovieUseCase getMovieUseCase;

    @MockitoBean private SearchMoviesUseCase searchMoviesUseCase;

    @MockitoBean private GetMoviesByCategoryUseCase getMoviesByCategoryUseCase;

    @MockitoBean private GetReadyMoviesUseCase getReadyMoviesUseCase;

    @MockitoBean private CreateMovieUseCase createMovieUseCase;

    @MockitoBean private UpdateMovieUseCase updateMovieUseCase;

    @MockitoBean private DeleteMovieUseCase deleteMovieUseCase;

    @MockitoBean private StartDownloadUseCase startDownloadUseCase;

    @MockitoBean private GetCacheStatsUseCase getCacheStatsUseCase;

    @MockitoBean private GetCachedMoviesUseCase getCachedMoviesUseCase;

    @MockitoBean private ClearMovieCacheUseCase clearMovieCacheUseCase;

    @MockitoBean private ClearAllCacheUseCase clearAllCacheUseCase;

    @MockitoBean private AddFavoriteUseCase addFavoriteUseCase;

    @MockitoBean private RemoveFavoriteUseCase removeFavoriteUseCase;

    @MockitoBean private GetFavoritesUseCase getFavoritesUseCase;

    @MockitoBean private GetMoviesGroupedUseCase getMoviesGroupedUseCase;

    @MockitoBean private GetAllCategoriesUseCase getAllCategoriesUseCase;

    @MockitoBean private MovieRestMapper movieRestMapper;

    private Movie entityMovie;
    private MovieResponseDTO movieResponseDto;

    @BeforeEach
    void setUp() {
        entityMovie =
                Movie.builder()
                        .id("movie-1")
                        .title("Test Movie")
                        .description("A test movie")
                        .year(2024)
                        .duration("2h 30m")
                        .status(MovieStatus.PENDING)
                        .createdAt(LocalDateTime.now())
                        .build();

        movieResponseDto =
                MovieResponseDTO.builder()
                        .id("movie-1")
                        .title("Test Movie")
                        .description("A test movie")
                        .year(2024)
                        .duration("2h 30m")
                        .status(MovieStatus.PENDING)
                        .categoryId("cat-1")
                        .categoryName("Action")
                        .cached(false)
                        .createdAt(LocalDateTime.now())
                        .build();
    }

    @Test
    void getAllMovies_shouldReturnMovieList() throws Exception {
        when(getAllMoviesUseCase.getAllMovies()).thenReturn(List.of(entityMovie));
        when(movieRestMapper.toResponse(entityMovie)).thenReturn(movieResponseDto);

        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("movie-1"))
                .andExpect(jsonPath("$[0].title").value("Test Movie"));

        verify(getAllMoviesUseCase).getAllMovies();
    }

    @Test
    void getAllMovies_withSearch_shouldCallSearchMovies() throws Exception {
        when(searchMoviesUseCase.searchMovies("test")).thenReturn(List.of(entityMovie));
        when(movieRestMapper.toResponse(entityMovie)).thenReturn(movieResponseDto);

        mockMvc.perform(get("/api/movies").param("search", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Movie"));

        verify(searchMoviesUseCase).searchMovies("test");
    }

    @Test
    void getAllMovies_withCategoryFilter_shouldCallGetMoviesByCategory() throws Exception {
        when(getMoviesByCategoryUseCase.getMoviesByCategory("cat-1"))
                .thenReturn(List.of(entityMovie));
        when(movieRestMapper.toResponse(entityMovie)).thenReturn(movieResponseDto);

        mockMvc.perform(get("/api/movies").param("categoryId", "cat-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryId").value("cat-1"));

        verify(getMoviesByCategoryUseCase).getMoviesByCategory("cat-1");
    }

    @Test
    void getAllMovies_withReadyOnly_shouldCallGetReadyMovies() throws Exception {
        Movie readyMovie =
                entityMovie.withStatus(MovieStatus.READY).withLocalPath("/cache/movie.mp4");

        MovieResponseDTO readyDto =
                movieResponseDto.toBuilder().status(MovieStatus.READY).cached(true).build();

        when(getReadyMoviesUseCase.getReadyMovies()).thenReturn(List.of(readyMovie));
        when(movieRestMapper.toResponse(readyMovie)).thenReturn(readyDto);

        mockMvc.perform(get("/api/movies").param("readyOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("READY"));

        verify(getReadyMoviesUseCase).getReadyMovies();
    }

    @Test
    void getMovie_shouldReturnMovie_whenExists() throws Exception {
        when(getMovieUseCase.getMovie("movie-1")).thenReturn(entityMovie);
        when(movieRestMapper.toResponse(entityMovie)).thenReturn(movieResponseDto);

        mockMvc.perform(get("/api/movies/movie-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("movie-1"))
                .andExpect(jsonPath("$.title").value("Test Movie"));

        verify(getMovieUseCase).getMovie("movie-1");
    }

    @Test
    void getMovie_shouldReturn404_whenNotFound() throws Exception {
        when(getMovieUseCase.getMovie("nonexistent"))
                .thenThrow(new MovieNotFoundException("nonexistent"));

        mockMvc.perform(get("/api/movies/nonexistent")).andExpect(status().isNotFound());
    }

    @Test
    void createMovie_shouldReturnCreatedMovie() throws Exception {
        MovieRequestDTO request =
                MovieRequestDTO.builder()
                        .title("New Movie")
                        .megaUrl("https://mega.nz/file/test")
                        .categoryId("cat-1")
                        .build();

        CreateMovieCommand command =
                CreateMovieCommand.builder()
                        .title("New Movie")
                        .megaUrl("https://mega.nz/file/test")
                        .categoryId("cat-1")
                        .build();

        when(movieRestMapper.toCreateCommand(request)).thenReturn(command);
        when(createMovieUseCase.createMovie(any(CreateMovieCommand.class))).thenReturn(entityMovie);
        when(movieRestMapper.toResponse(entityMovie)).thenReturn(movieResponseDto);

        mockMvc.perform(
                        post("/api/movies")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("movie-1"));

        verify(movieRestMapper).toCreateCommand(request);
        verify(createMovieUseCase).createMovie(any(CreateMovieCommand.class));
    }

    @Test
    void createMovie_shouldReturn400_whenTitleMissing() throws Exception {
        MovieRequestDTO request =
                MovieRequestDTO.builder().megaUrl("https://mega.nz/file/test").build();

        mockMvc.perform(
                        post("/api/movies")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMovie_shouldReturn400_whenMegaUrlMissing() throws Exception {
        MovieRequestDTO request = MovieRequestDTO.builder().title("New Movie").build();

        mockMvc.perform(
                        post("/api/movies")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMovie_shouldReturnUpdatedMovie() throws Exception {
        MovieRequestDTO request =
                MovieRequestDTO.builder()
                        .title("Updated Movie")
                        .megaUrl("https://mega.nz/file/test")
                        .build();

        Movie updatedMovie = entityMovie.withTitle("Updated Movie");

        MovieResponseDTO updatedDto = movieResponseDto.toBuilder().title("Updated Movie").build();

        UpdateMovieCommand command =
                UpdateMovieCommand.builder()
                        .id("movie-1")
                        .title("Updated Movie")
                        .megaUrl("https://mega.nz/file/test")
                        .build();

        when(movieRestMapper.toUpdateCommand("movie-1", request)).thenReturn(command);
        when(updateMovieUseCase.updateMovie(any(UpdateMovieCommand.class)))
                .thenReturn(updatedMovie);
        when(movieRestMapper.toResponse(updatedMovie)).thenReturn(updatedDto);

        mockMvc.perform(
                        put("/api/movies/movie-1")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Movie"));

        verify(updateMovieUseCase).updateMovie(any(UpdateMovieCommand.class));
    }

    @Test
    void deleteMovie_shouldReturn204() throws Exception {
        doNothing().when(deleteMovieUseCase).deleteMovie("movie-1");

        mockMvc.perform(delete("/api/movies/movie-1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(deleteMovieUseCase).deleteMovie("movie-1");
    }

    @Test
    void startDownload_shouldReturn202() throws Exception {
        doNothing().when(startDownloadUseCase).startDownload("movie-1");

        mockMvc.perform(post("/api/movies/movie-1/download").with(csrf()))
                .andExpect(status().isAccepted());

        verify(startDownloadUseCase).startDownload("movie-1");
    }

    @Test
    void getCacheStats_shouldReturnStats() throws Exception {
        var cacheStats =
                com.mediaserver.application.port.in.CacheManagementUseCase.CacheStats.builder()
                        .totalSizeBytes(1024L * 1024 * 1024)
                        .maxSizeBytes(100L * 1024 * 1024 * 1024)
                        .usagePercent(1)
                        .movieCount(5)
                        .build();

        when(getCacheStatsUseCase.getCacheStats()).thenReturn(cacheStats);

        mockMvc.perform(get("/api/movies/cache/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movieCount").value(5))
                .andExpect(jsonPath("$.usagePercent").value(1));
    }

    @Test
    void getCachedMovies_shouldReturnCachedMovies() throws Exception {
        Movie cachedMovie =
                entityMovie.withLocalPath("/cache/movie.mp4").withStatus(MovieStatus.READY);

        MovieResponseDTO cachedDto =
                movieResponseDto.toBuilder().cached(true).status(MovieStatus.READY).build();

        when(getCachedMoviesUseCase.getCachedMovies()).thenReturn(List.of(cachedMovie));
        when(movieRestMapper.toResponse(cachedMovie)).thenReturn(cachedDto);

        mockMvc.perform(get("/api/movies/cached"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cached").value(true));

        verify(getCachedMoviesUseCase).getCachedMovies();
    }

    @Test
    void clearMovieCache_shouldReturn204() throws Exception {
        doNothing().when(clearMovieCacheUseCase).clearCache("movie-1");

        mockMvc.perform(delete("/api/movies/movie-1/cache").with(csrf()))
                .andExpect(status().isNoContent());

        verify(clearMovieCacheUseCase).clearCache("movie-1");
    }

    @Test
    void clearAllCache_shouldReturnClearedCount() throws Exception {
        when(clearAllCacheUseCase.clearAllCache()).thenReturn(5);

        mockMvc.perform(delete("/api/movies/cache").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));

        verify(clearAllCacheUseCase).clearAllCache();
    }

    @Test
    void getFavorites_shouldReturnFavoriteMovies() throws Exception {
        when(getFavoritesUseCase.getFavorites()).thenReturn(List.of(entityMovie));
        when(movieRestMapper.toResponse(entityMovie)).thenReturn(movieResponseDto);

        mockMvc.perform(get("/api/movies/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("movie-1"));

        verify(getFavoritesUseCase).getFavorites();
    }

    @Test
    void addFavorite_shouldReturnUpdatedMovie() throws Exception {
        when(addFavoriteUseCase.addFavorite("movie-1")).thenReturn(entityMovie);
        when(movieRestMapper.toResponse(entityMovie)).thenReturn(movieResponseDto);

        mockMvc.perform(post("/api/movies/movie-1/favorite").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("movie-1"));

        verify(addFavoriteUseCase).addFavorite("movie-1");
    }

    @Test
    void removeFavorite_shouldReturnUpdatedMovie() throws Exception {
        when(removeFavoriteUseCase.removeFavorite("movie-1")).thenReturn(entityMovie);
        when(movieRestMapper.toResponse(entityMovie)).thenReturn(movieResponseDto);

        mockMvc.perform(delete("/api/movies/movie-1/favorite").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("movie-1"));

        verify(removeFavoriteUseCase).removeFavorite("movie-1");
    }
}
