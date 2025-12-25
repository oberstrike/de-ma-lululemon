package com.mediaserver.infrastructure.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.mediaserver.domain.model.Movie;
import com.mediaserver.domain.model.MovieStatus;
import com.mediaserver.infrastructure.persistence.entity.MovieJpaEntity;
import com.mediaserver.infrastructure.persistence.mapper.MoviePersistenceMapper;
import com.mediaserver.infrastructure.persistence.repository.JpaCategoryRepository;
import com.mediaserver.infrastructure.persistence.repository.JpaMovieRepository;
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
 * Unit tests for MovieRepositoryAdapter.
 * Tests the adapter implementation that bridges domain and persistence layers.
 */
@ExtendWith(MockitoExtension.class)
class MovieRepositoryAdapterTest {

    @Mock
    private JpaMovieRepository jpaMovieRepository;

    @Mock
    private JpaCategoryRepository jpaCategoryRepository;

    @Mock
    private MoviePersistenceMapper mapper;

    @InjectMocks
    private MovieRepositoryAdapter movieRepositoryAdapter;

    private Movie domainMovie;
    private MovieJpaEntity entityMovie;

    @BeforeEach
    void setUp() {
        domainMovie = Movie.builder()
                .id("movie-1")
                .title("Test Movie")
                .description("A test movie")
                .year(2024)
                .status(MovieStatus.PENDING)
                .categoryId("cat-1")
                .createdAt(LocalDateTime.now())
                .build();

        entityMovie = MovieJpaEntity.builder()
                .id("movie-1")
                .title("Test Movie")
                .description("A test movie")
                .year(2024)
                .status(MovieStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void findAll_shouldMapEntitiesToDomainModels() {
        // Given
        when(jpaMovieRepository.findAll()).thenReturn(List.of(entityMovie));
        when(mapper.toDomainList(List.of(entityMovie))).thenReturn(List.of(domainMovie));

        // When
        List<Movie> result = movieRepositoryAdapter.findAll();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Movie");
        verify(jpaMovieRepository).findAll();
        verify(mapper).toDomainList(List.of(entityMovie));
    }

    @Test
    void findById_shouldReturnDomainModel_whenExists() {
        // Given
        when(jpaMovieRepository.findById("movie-1")).thenReturn(Optional.of(entityMovie));
        when(mapper.toDomain(entityMovie)).thenReturn(domainMovie);

        // When
        Optional<Movie> result = movieRepositoryAdapter.findById("movie-1");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("movie-1");
        assertThat(result.get().getTitle()).isEqualTo("Test Movie");
        verify(jpaMovieRepository).findById("movie-1");
        verify(mapper).toDomain(entityMovie);
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        // Given
        when(jpaMovieRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When
        Optional<Movie> result = movieRepositoryAdapter.findById("nonexistent");

        // Then
        assertThat(result).isEmpty();
        verify(jpaMovieRepository).findById("nonexistent");
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void save_shouldMapToEntityAndSave() {
        // Given
        when(mapper.toEntity(domainMovie)).thenReturn(entityMovie);
        when(jpaMovieRepository.save(any(MovieJpaEntity.class))).thenReturn(entityMovie);
        when(mapper.toDomain(entityMovie)).thenReturn(domainMovie);

        // When
        Movie result = movieRepositoryAdapter.save(domainMovie);

        // Then
        assertThat(result.getId()).isEqualTo("movie-1");
        assertThat(result.getTitle()).isEqualTo("Test Movie");
        verify(mapper).toEntity(domainMovie);
        verify(jpaMovieRepository).save(any(MovieJpaEntity.class));
        verify(mapper).toDomain(entityMovie);
    }

    @Test
    void delete_shouldCallRepositoryDelete() {
        // Given
        doNothing().when(jpaMovieRepository).deleteById("movie-1");

        // When
        movieRepositoryAdapter.delete("movie-1");

        // Then
        verify(jpaMovieRepository).deleteById("movie-1");
    }

    @Test
    void search_shouldMapSearchResults() {
        // Given
        when(jpaMovieRepository.search("test")).thenReturn(List.of(entityMovie));
        when(mapper.toDomainList(List.of(entityMovie))).thenReturn(List.of(domainMovie));

        // When
        List<Movie> result = movieRepositoryAdapter.search("test");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Movie");
        verify(jpaMovieRepository).search("test");
        verify(mapper).toDomainList(List.of(entityMovie));
    }

    @Test
    void findByStatus_shouldMapMoviesWithGivenStatus() {
        // Given
        when(jpaMovieRepository.findByStatus(MovieStatus.READY)).thenReturn(List.of(entityMovie));
        when(mapper.toDomainList(List.of(entityMovie))).thenReturn(List.of(domainMovie));

        // When
        List<Movie> result = movieRepositoryAdapter.findByStatus(MovieStatus.READY);

        // Then
        assertThat(result).hasSize(1);
        verify(jpaMovieRepository).findByStatus(MovieStatus.READY);
        verify(mapper).toDomainList(List.of(entityMovie));
    }

    @Test
    void findByCategoryId_shouldMapMoviesInCategory() {
        // Given
        when(jpaMovieRepository.findByCategoryId("cat-1")).thenReturn(List.of(entityMovie));
        when(mapper.toDomainList(List.of(entityMovie))).thenReturn(List.of(domainMovie));

        // When
        List<Movie> result = movieRepositoryAdapter.findByCategoryId("cat-1");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategoryId()).isEqualTo("cat-1");
        verify(jpaMovieRepository).findByCategoryId("cat-1");
        verify(mapper).toDomainList(List.of(entityMovie));
    }

    @Test
    void findCachedMovies_shouldReturnCachedMovies() {
        // Given
        Movie cachedMovie = domainMovie
                .withLocalPath("/cache/movie.mp4")
                .withStatus(MovieStatus.READY);

        MovieJpaEntity cachedEntity = MovieJpaEntity.builder()
                .id("movie-1")
                .title("Test Movie")
                .description("A test movie")
                .year(2024)
                .status(MovieStatus.READY)
                .localPath("/cache/movie.mp4")
                .createdAt(LocalDateTime.now())
                .build();

        when(jpaMovieRepository.findCachedMovies()).thenReturn(List.of(cachedEntity));
        when(mapper.toDomainList(List.of(cachedEntity))).thenReturn(List.of(cachedMovie));

        // When
        List<Movie> result = movieRepositoryAdapter.findCachedMovies();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).isCached()).isTrue();
        verify(jpaMovieRepository).findCachedMovies();
        verify(mapper).toDomainList(List.of(cachedEntity));
    }

    @Test
    void getTotalCacheSize_shouldReturnSizeFromRepository() {
        // Given
        when(jpaMovieRepository.getTotalCacheSize()).thenReturn(5L * 1024 * 1024 * 1024);

        // When
        Long result = movieRepositoryAdapter.getTotalCacheSize();

        // Then
        assertThat(result).isEqualTo(5L * 1024 * 1024 * 1024);
        verify(jpaMovieRepository).getTotalCacheSize();
    }

    @Test
    void countCached_shouldReturnCountFromRepository() {
        // Given
        when(jpaMovieRepository.countByLocalPathIsNotNull()).thenReturn(10L);

        // When
        long result = movieRepositoryAdapter.countCached();

        // Then
        assertThat(result).isEqualTo(10L);
        verify(jpaMovieRepository).countByLocalPathIsNotNull();
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoMovies() {
        // Given
        when(jpaMovieRepository.findAll()).thenReturn(List.of());
        when(mapper.toDomainList(List.of())).thenReturn(List.of());

        // When
        List<Movie> result = movieRepositoryAdapter.findAll();

        // Then
        assertThat(result).isEmpty();
        verify(jpaMovieRepository).findAll();
    }

    @Test
    void save_shouldHandleNullCategoryCorrectly() {
        // Given
        Movie movieWithoutCategory = domainMovie.withCategoryId(null);

        MovieJpaEntity entityWithoutCategory = MovieJpaEntity.builder()
                .id("movie-1")
                .title("Test Movie")
                .description("A test movie")
                .year(2024)
                .status(MovieStatus.PENDING)
                .category(null)
                .createdAt(LocalDateTime.now())
                .build();

        when(mapper.toEntity(movieWithoutCategory)).thenReturn(entityWithoutCategory);
        when(jpaMovieRepository.save(any(MovieJpaEntity.class))).thenReturn(entityWithoutCategory);
        when(mapper.toDomain(entityWithoutCategory)).thenReturn(movieWithoutCategory);

        // When
        Movie result = movieRepositoryAdapter.save(movieWithoutCategory);

        // Then
        assertThat(result.getCategoryId()).isNull();
        verify(mapper).toEntity(movieWithoutCategory);
        verify(jpaMovieRepository).save(any(MovieJpaEntity.class));
    }
}
