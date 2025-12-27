package com.mediaserver.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.mediaserver.application.command.CreateMovieCommand;
import com.mediaserver.application.command.UpdateMovieCommand;
import com.mediaserver.application.port.in.CacheManagementUseCase.CacheStats;
import com.mediaserver.application.port.out.CategoryPort;
import com.mediaserver.application.port.out.CurrentUserProvider;
import com.mediaserver.application.port.out.DownloadServicePort;
import com.mediaserver.application.port.out.FileStoragePort;
import com.mediaserver.application.port.out.MoviePort;
import com.mediaserver.config.MediaProperties;
import com.mediaserver.domain.model.Category;
import com.mediaserver.domain.model.Movie;
import com.mediaserver.domain.model.MovieStatus;
import com.mediaserver.exception.MovieNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for MovieApplicationService. Tests use case orchestration with mocked output ports.
 */
@ExtendWith(MockitoExtension.class)
class MovieApplicationServiceTest {

    @Mock private MoviePort moviePort;

    @Mock private CategoryPort categoryPort;

    @Mock private FileStoragePort fileStoragePort;

    @Mock private DownloadServicePort downloadServicePort;

    @Mock private CurrentUserProvider currentUserProvider;

    @Mock private MediaProperties properties;

    @InjectMocks private MovieApplicationService movieApplicationService;

    private Movie testMovie;

    @BeforeEach
    void setUp() {
        testMovie =
                Movie.builder()
                        .id("movie-1")
                        .title("Test Movie")
                        .description("A test movie")
                        .year(2024)
                        .duration("2h 30m")
                        .megaUrl("https://mega.nz/file/test")
                        .status(MovieStatus.PENDING)
                        .categoryId("cat-1")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
        lenient().when(currentUserProvider.getCurrentUserId()).thenReturn("user-1");
        lenient().when(moviePort.findFavorites("user-1")).thenReturn(List.of());
        lenient().when(moviePort.isFavorite(anyString(), anyString())).thenReturn(false);
        lenient()
                .when(moviePort.applyFavoriteStatus(any(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void getAllMovies_shouldReturnAllMovies() {
        List<Movie> movies = List.of(testMovie);
        when(moviePort.findAll()).thenReturn(movies);

        List<Movie> result = movieApplicationService.getAllMovies();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Movie");
        verify(moviePort).findAll();
        verify(moviePort).applyFavoriteStatus(movies, "user-1");
    }

    @Test
    void getMovie_shouldReturnMovie_whenExists() {
        when(moviePort.findById("movie-1")).thenReturn(Optional.of(testMovie));

        Movie result = movieApplicationService.getMovie("movie-1");

        assertThat(result.getId()).isEqualTo("movie-1");
        assertThat(result.getTitle()).isEqualTo("Test Movie");
        verify(moviePort).findById("movie-1");
        verify(moviePort).isFavorite("movie-1", "user-1");
    }

    @Test
    void getMovie_shouldThrowException_whenNotFound() {
        when(moviePort.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieApplicationService.getMovie("nonexistent"))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessageContaining("nonexistent");
        verify(moviePort).findById("nonexistent");
    }

    @Test
    void createMovie_shouldSaveAndReturnMovie() {
        CreateMovieCommand command =
                CreateMovieCommand.builder()
                        .title("New Movie")
                        .description("A new movie")
                        .megaUrl("https://mega.nz/file/new")
                        .categoryId("cat-1")
                        .build();

        Category category = Category.builder().id("cat-1").name("Action").build();

        Movie savedMovie =
                Movie.builder()
                        .id("new-movie-id")
                        .title("New Movie")
                        .description("A new movie")
                        .megaUrl("https://mega.nz/file/new")
                        .status(MovieStatus.PENDING)
                        .categoryId("cat-1")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

        when(categoryPort.findById("cat-1")).thenReturn(Optional.of(category));
        when(moviePort.save(any(Movie.class))).thenReturn(savedMovie);

        Movie result = movieApplicationService.createMovie(command);

        assertThat(result.getId()).isEqualTo("new-movie-id");
        assertThat(result.getTitle()).isEqualTo("New Movie");
        assertThat(result.getStatus()).isEqualTo(MovieStatus.PENDING);
        verify(categoryPort).findById("cat-1");
        verify(moviePort).save(any(Movie.class));
    }

    @Test
    void updateMovie_shouldUpdateAndReturnMovie() {
        UpdateMovieCommand command =
                UpdateMovieCommand.builder()
                        .title("Updated Title")
                        .description("Updated description")
                        .categoryId("cat-1")
                        .build();

        Category category = Category.builder().id("cat-1").name("Action").build();

        Movie updatedMovie =
                testMovie
                        .withTitle("Updated Title")
                        .withDescription("Updated description")
                        .withUpdatedAt(LocalDateTime.now());

        when(moviePort.findById("movie-1")).thenReturn(Optional.of(testMovie));
        when(categoryPort.findById("cat-1")).thenReturn(Optional.of(category));
        when(moviePort.save(any(Movie.class))).thenReturn(updatedMovie);

        Movie result = movieApplicationService.updateMovie("movie-1", command);

        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.getDescription()).isEqualTo("Updated description");
        verify(moviePort).findById("movie-1");
        verify(categoryPort).findById("cat-1");
        verify(moviePort).save(any(Movie.class));
    }

    @Test
    void updateMovie_shouldThrowException_whenNotFound() {
        UpdateMovieCommand command = UpdateMovieCommand.builder().title("Updated").build();
        when(moviePort.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieApplicationService.updateMovie("nonexistent", command))
                .isInstanceOf(MovieNotFoundException.class);
        verify(moviePort).findById("nonexistent");
        verify(moviePort, never()).save(any());
    }

    @Test
    void deleteMovie_shouldCallPortDelete_whenExists() {
        when(moviePort.findById("movie-1")).thenReturn(Optional.of(testMovie));
        doNothing().when(moviePort).delete(testMovie);

        movieApplicationService.deleteMovie("movie-1");

        verify(moviePort).findById("movie-1");
        verify(moviePort).delete(testMovie);
    }

    @Test
    void deleteMovie_shouldThrowException_whenNotFound() {
        when(moviePort.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieApplicationService.deleteMovie("nonexistent"))
                .isInstanceOf(MovieNotFoundException.class);
        verify(moviePort).findById("nonexistent");
        verify(moviePort, never()).delete(any(Movie.class));
    }

    @Test
    void searchMovies_shouldReturnMatchingMovies() {
        List<Movie> movies = List.of(testMovie);
        when(moviePort.search("test")).thenReturn(movies);

        List<Movie> result = movieApplicationService.searchMovies("test");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Movie");
        verify(moviePort).search("test");
        verify(moviePort).applyFavoriteStatus(movies, "user-1");
    }

    @Test
    void getReadyMovies_shouldReturnOnlyReadyMovies() {
        Movie readyMovie =
                testMovie.withStatus(MovieStatus.READY).withLocalPath("/path/to/video.mp4");
        List<Movie> movies = List.of(readyMovie);

        when(moviePort.findReadyMovies()).thenReturn(movies);

        List<Movie> result = movieApplicationService.getReadyMovies();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(MovieStatus.READY);
        assertThat(result.get(0).isCached()).isTrue();
        verify(moviePort).findReadyMovies();
        verify(moviePort).applyFavoriteStatus(movies, "user-1");
    }

    @Test
    void getMoviesByCategory_shouldReturnMoviesInCategory() {
        List<Movie> movies = List.of(testMovie);
        when(moviePort.findByCategoryId("cat-1")).thenReturn(movies);

        List<Movie> result = movieApplicationService.getMoviesByCategory("cat-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategoryId()).isEqualTo("cat-1");
        verify(moviePort).findByCategoryId("cat-1");
        verify(moviePort).applyFavoriteStatus(movies, "user-1");
    }

    @Test
    void getCachedMovies_shouldReturnCachedMovies() {
        Movie cachedMovie =
                testMovie.withStatus(MovieStatus.READY).withLocalPath("/cache/movie.mp4");
        List<Movie> movies = List.of(cachedMovie);

        when(moviePort.findCachedMovies()).thenReturn(movies);

        List<Movie> result = movieApplicationService.getCachedMovies();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isCached()).isTrue();
        verify(moviePort).findCachedMovies();
        verify(moviePort).applyFavoriteStatus(movies, "user-1");
    }

    @Test
    void startDownload_shouldUpdateStatusToDownloading() {
        when(moviePort.findById("movie-1")).thenReturn(Optional.of(testMovie));

        Movie downloadingMovie = testMovie.withStatus(MovieStatus.DOWNLOADING);

        when(moviePort.save(any(Movie.class))).thenReturn(downloadingMovie);
        when(downloadServicePort.downloadMovie(any(Movie.class)))
                .thenReturn(
                        java.util.concurrent.CompletableFuture.completedFuture(
                                java.nio.file.Path.of("/cache/movie.mp4")));

        movieApplicationService.startDownload("movie-1");

        verify(moviePort).findById("movie-1");
        verify(moviePort).save(argThat(movie -> movie.getStatus() == MovieStatus.DOWNLOADING));
        verify(downloadServicePort).downloadMovie(any(Movie.class));
    }

    @Test
    void addFavorite_shouldPersistFavoriteForUser() {
        when(moviePort.findById("movie-1")).thenReturn(Optional.of(testMovie));
        when(moviePort.isFavorite("movie-1", "user-1")).thenReturn(false);

        Movie result = movieApplicationService.addFavorite("movie-1");

        assertThat(result.isFavorite()).isTrue();
        verify(moviePort).addFavorite("movie-1", "user-1");
    }

    @Test
    void removeFavorite_shouldRemoveFavoriteForUser() {
        when(moviePort.findById("movie-1")).thenReturn(Optional.of(testMovie));
        when(moviePort.isFavorite("movie-1", "user-1")).thenReturn(true);

        Movie result = movieApplicationService.removeFavorite("movie-1");

        assertThat(result.isFavorite()).isFalse();
        verify(moviePort).removeFavorite("movie-1", "user-1");
    }

    @Test
    void getFavorites_shouldReturnFavoritesForUser() {
        Movie favoriteMovie = testMovie.withFavorite(true);
        when(moviePort.findFavorites("user-1")).thenReturn(List.of(favoriteMovie));

        List<Movie> result = movieApplicationService.getFavorites();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isFavorite()).isTrue();
        verify(moviePort).findFavorites("user-1");
    }

    @Test
    void startDownload_shouldThrowException_whenAlreadyCached() {
        Movie cachedMovie =
                testMovie.withStatus(MovieStatus.READY).withLocalPath("/cache/movie.mp4");

        when(moviePort.findById("movie-1")).thenReturn(Optional.of(cachedMovie));

        assertThatThrownBy(() -> movieApplicationService.startDownload("movie-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already downloaded");

        verify(moviePort).findById("movie-1");
        verify(moviePort, never()).save(any());
    }

    @Test
    void clearCache_shouldClearLocalPathAndResetStatus() throws Exception {
        Movie cachedMovie =
                testMovie
                        .withStatus(MovieStatus.READY)
                        .withLocalPath("/cache/movie.mp4")
                        .withFileSize(1024L * 1024 * 1024);

        when(moviePort.findById("movie-1")).thenReturn(Optional.of(cachedMovie));

        Movie clearedMovie =
                cachedMovie
                        .withLocalPath(null)
                        .withFileSize(null)
                        .withStatus(MovieStatus.PENDING)
                        .withUpdatedAt(LocalDateTime.now());

        when(fileStoragePort.deleteIfExists(any())).thenReturn(true);
        when(moviePort.save(any(Movie.class))).thenReturn(clearedMovie);

        movieApplicationService.clearCache("movie-1");

        verify(moviePort).findById("movie-1");
        verify(fileStoragePort).deleteIfExists(any());
        verify(moviePort)
                .save(
                        argThat(
                                movie ->
                                        movie.getLocalPath() == null
                                                && movie.getFileSize() == null
                                                && movie.getStatus() == MovieStatus.PENDING));
    }

    @Test
    void getCacheStats_shouldReturnCacheStatistics() {
        MediaProperties.Storage storage = new MediaProperties.Storage();
        storage.setMaxCacheSizeGb(10);

        when(properties.getStorage()).thenReturn(storage);
        when(moviePort.getTotalCacheSize()).thenReturn(5L * 1024 * 1024 * 1024);
        when(moviePort.countCachedMovies()).thenReturn(10L);

        CacheStats result = movieApplicationService.getCacheStats();

        assertThat(result.getTotalSizeBytes()).isEqualTo(5L * 1024 * 1024 * 1024);
        assertThat(result.getMaxSizeBytes()).isEqualTo(10L * 1024 * 1024 * 1024);
        assertThat(result.getUsagePercent()).isEqualTo(50);
        assertThat(result.getMovieCount()).isEqualTo(10L);
        verify(moviePort).getTotalCacheSize();
        verify(moviePort).countCachedMovies();
        verify(properties).getStorage();
    }
}
