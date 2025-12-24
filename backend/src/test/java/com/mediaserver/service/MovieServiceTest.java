package com.mediaserver.service;

import com.mediaserver.config.MediaProperties;
import com.mediaserver.dto.MovieCreateRequest;
import com.mediaserver.dto.MovieDto;
import com.mediaserver.dto.MovieMapper;
import com.mediaserver.entity.Category;
import com.mediaserver.entity.Movie;
import com.mediaserver.entity.MovieStatus;
import com.mediaserver.exception.CategoryNotFoundException;
import com.mediaserver.exception.MovieNotFoundException;
import com.mediaserver.repository.CategoryRepository;
import com.mediaserver.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MegaDownloadService downloadService;

    @Mock
    private MovieMapper movieMapper;

    @Mock
    private MediaProperties properties;

    @InjectMocks
    private MovieService movieService;

    private Movie testMovie;
    private MovieDto testMovieDto;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id("cat-1")
                .name("Action")
                .build();

        testMovie = Movie.builder()
                .id("movie-1")
                .title("Test Movie")
                .description("A test movie")
                .year(2024)
                .duration("2h 30m")
                .megaUrl("https://mega.nz/file/test")
                .status(MovieStatus.PENDING)
                .category(testCategory)
                .createdAt(LocalDateTime.now())
                .build();

        testMovieDto = MovieDto.builder()
                .id("movie-1")
                .title("Test Movie")
                .description("A test movie")
                .year(2024)
                .duration("2h 30m")
                .status(MovieStatus.PENDING)
                .categoryId("cat-1")
                .categoryName("Action")
                .cached(false)
                .build();
    }

    @Test
    void getAllMovies_shouldReturnAllMovies() {
        when(movieRepository.findAll()).thenReturn(List.of(testMovie));
        when(movieMapper.toDto(testMovie)).thenReturn(testMovieDto);

        List<MovieDto> result = movieService.getAllMovies();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Movie");
        verify(movieRepository).findAll();
    }

    @Test
    void getMovie_shouldReturnMovie_whenExists() {
        when(movieRepository.findById("movie-1")).thenReturn(Optional.of(testMovie));
        when(movieMapper.toDto(testMovie)).thenReturn(testMovieDto);

        MovieDto result = movieService.getMovie("movie-1");

        assertThat(result.getId()).isEqualTo("movie-1");
        assertThat(result.getTitle()).isEqualTo("Test Movie");
    }

    @Test
    void getMovie_shouldThrowException_whenNotFound() {
        when(movieRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.getMovie("nonexistent"))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessageContaining("nonexistent");
    }

    @Test
    void createMovie_shouldCreateAndReturnMovie() {
        MovieCreateRequest request = MovieCreateRequest.builder()
                .title("New Movie")
                .description("A new movie")
                .megaUrl("https://mega.nz/file/new")
                .categoryId("cat-1")
                .build();

        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(testCategory));
        when(movieRepository.save(any(Movie.class))).thenAnswer(inv -> {
            Movie m = inv.getArgument(0);
            m.setId("new-movie-id");
            return m;
        });
        when(movieMapper.toDto(any(Movie.class))).thenReturn(
                MovieDto.builder().id("new-movie-id").title("New Movie").build()
        );

        MovieDto result = movieService.createMovie(request);

        assertThat(result.getId()).isEqualTo("new-movie-id");
        verify(movieRepository).save(any(Movie.class));
    }

    @Test
    void createMovie_shouldThrowException_whenCategoryNotFound() {
        MovieCreateRequest request = MovieCreateRequest.builder()
                .title("New Movie")
                .megaUrl("https://mega.nz/file/new")
                .categoryId("invalid-cat")
                .build();

        when(categoryRepository.findById("invalid-cat")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.createMovie(request))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void searchMovies_shouldReturnMatchingMovies() {
        when(movieRepository.search("test")).thenReturn(List.of(testMovie));
        when(movieMapper.toDto(testMovie)).thenReturn(testMovieDto);

        List<MovieDto> result = movieService.searchMovies("test");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Movie");
    }

    @Test
    void deleteMovie_shouldDeleteMovie_whenExists() {
        when(movieRepository.findById("movie-1")).thenReturn(Optional.of(testMovie));

        movieService.deleteMovie("movie-1");

        verify(movieRepository).delete(testMovie);
    }

    @Test
    void deleteMovie_shouldThrowException_whenNotFound() {
        when(movieRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.deleteMovie("nonexistent"))
                .isInstanceOf(MovieNotFoundException.class);
    }

    @Test
    void startDownload_shouldInitiateDownload() {
        testMovie.setStatus(MovieStatus.PENDING);
        when(movieRepository.findById("movie-1")).thenReturn(Optional.of(testMovie));
        when(movieRepository.save(any(Movie.class))).thenReturn(testMovie);

        movieService.startDownload("movie-1");

        assertThat(testMovie.getStatus()).isEqualTo(MovieStatus.DOWNLOADING);
        verify(downloadService).downloadMovie(testMovie);
    }

    @Test
    void startDownload_shouldThrowException_whenAlreadyCached() {
        testMovie.setStatus(MovieStatus.READY);
        testMovie.setLocalPath("/path/to/file.mp4");
        when(movieRepository.findById("movie-1")).thenReturn(Optional.of(testMovie));

        assertThatThrownBy(() -> movieService.startDownload("movie-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already downloaded");
    }

    @Test
    void getReadyMovies_shouldReturnOnlyReadyMovies() {
        Movie readyMovie = Movie.builder()
                .id("ready-1")
                .title("Ready Movie")
                .status(MovieStatus.READY)
                .localPath("/path/to/video.mp4")
                .build();

        when(movieRepository.findReadyMovies()).thenReturn(List.of(readyMovie));
        when(movieMapper.toDto(readyMovie)).thenReturn(
                MovieDto.builder().id("ready-1").status(MovieStatus.READY).cached(true).build()
        );

        List<MovieDto> result = movieService.getReadyMovies();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(MovieStatus.READY);
    }
}
