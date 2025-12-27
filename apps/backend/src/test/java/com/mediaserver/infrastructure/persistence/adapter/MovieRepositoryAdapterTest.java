package com.mediaserver.infrastructure.persistence.adapter;

import com.mediaserver.domain.model.Movie;
import com.mediaserver.domain.model.MovieStatus;
import com.mediaserver.infrastructure.persistence.entity.MovieFavoriteJpaEntity;
import com.mediaserver.infrastructure.persistence.entity.MovieJpaEntity;
import com.mediaserver.infrastructure.persistence.mapper.MoviePersistenceMapper;
import com.mediaserver.infrastructure.persistence.repository.JpaCategoryRepository;
import com.mediaserver.infrastructure.persistence.repository.JpaMovieFavoriteRepository;
import com.mediaserver.infrastructure.persistence.repository.JpaMovieRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MovieRepositoryAdapter. Tests the adapter implementation that bridges domain and
 * persistence layers.
 */
@ExtendWith(MockitoExtension.class)
class MovieRepositoryAdapterTest {

    @Mock private JpaMovieRepository jpaMovieRepository;

    @Mock private JpaCategoryRepository jpaCategoryRepository;

    @Mock private JpaMovieFavoriteRepository jpaMovieFavoriteRepository;

    @Mock private MoviePersistenceMapper mapper;

    @InjectMocks private MovieRepositoryAdapter movieRepositoryAdapter;

    private Movie domainMovie;
    private MovieJpaEntity entityMovie;

    @BeforeEach
    void setUp() {
        domainMovie =
                Movie.builder()
                        .id("movie-1")
                        .title("Test Movie")
                        .description("A test movie")
                        .year(2024)
                        .status(MovieStatus.PENDING)
                        .categoryId("cat-1")
                        .createdAt(LocalDateTime.now())
                        .build();

        entityMovie =
                MovieJpaEntity.builder()
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
        when(jpaMovieRepository.findAll()).thenReturn(List.of(entityMovie));
        when(mapper.toDomainList(List.of(entityMovie))).thenReturn(List.of(domainMovie));

        List<Movie> result = movieRepositoryAdapter.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Movie");
        verify(jpaMovieRepository).findAll();
        verify(mapper).toDomainList(List.of(entityMovie));
    }

    @Test
    void findById_shouldReturnDomainModel_whenExists() {
        when(jpaMovieRepository.findById("movie-1")).thenReturn(Optional.of(entityMovie));
        when(mapper.toDomain(entityMovie)).thenReturn(domainMovie);

        Optional<Movie> result = movieRepositoryAdapter.findById("movie-1");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("movie-1");
        assertThat(result.get().getTitle()).isEqualTo("Test Movie");
        verify(jpaMovieRepository).findById("movie-1");
        verify(mapper).toDomain(entityMovie);
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        when(jpaMovieRepository.findById("nonexistent")).thenReturn(Optional.empty());

        Optional<Movie> result = movieRepositoryAdapter.findById("nonexistent");

        assertThat(result).isEmpty();
        verify(jpaMovieRepository).findById("nonexistent");
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void save_shouldMapToEntityAndSave() {
        when(mapper.toEntity(domainMovie)).thenReturn(entityMovie);
        when(jpaMovieRepository.save(any(MovieJpaEntity.class))).thenReturn(entityMovie);
        when(mapper.toDomain(entityMovie)).thenReturn(domainMovie);

        Movie result = movieRepositoryAdapter.save(domainMovie);

        assertThat(result.getId()).isEqualTo("movie-1");
        assertThat(result.getTitle()).isEqualTo("Test Movie");
        verify(mapper).toEntity(domainMovie);
        verify(jpaMovieRepository).save(any(MovieJpaEntity.class));
        verify(mapper).toDomain(entityMovie);
    }

    @Test
    void delete_shouldCallRepositoryDelete() {
        doNothing().when(jpaMovieRepository).deleteById("movie-1");

        movieRepositoryAdapter.delete("movie-1");

        verify(jpaMovieRepository).deleteById("movie-1");
    }

    @Test
    void search_shouldMapSearchResults() {
        when(jpaMovieRepository.search("test")).thenReturn(List.of(entityMovie));
        when(mapper.toDomainList(List.of(entityMovie))).thenReturn(List.of(domainMovie));

        List<Movie> result = movieRepositoryAdapter.search("test");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Movie");
        verify(jpaMovieRepository).search("test");
        verify(mapper).toDomainList(List.of(entityMovie));
    }

    @Test
    void findByStatus_shouldMapMoviesWithGivenStatus() {
        when(jpaMovieRepository.findByStatus(MovieStatus.READY)).thenReturn(List.of(entityMovie));
        when(mapper.toDomainList(List.of(entityMovie))).thenReturn(List.of(domainMovie));

        List<Movie> result = movieRepositoryAdapter.findByStatus(MovieStatus.READY);

        assertThat(result).hasSize(1);
        verify(jpaMovieRepository).findByStatus(MovieStatus.READY);
        verify(mapper).toDomainList(List.of(entityMovie));
    }

    @Test
    void findByCategoryId_shouldMapMoviesInCategory() {
        when(jpaMovieRepository.findByCategoryId("cat-1")).thenReturn(List.of(entityMovie));
        when(mapper.toDomainList(List.of(entityMovie))).thenReturn(List.of(domainMovie));

        List<Movie> result = movieRepositoryAdapter.findByCategoryId("cat-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategoryId()).isEqualTo("cat-1");
        verify(jpaMovieRepository).findByCategoryId("cat-1");
        verify(mapper).toDomainList(List.of(entityMovie));
    }

    @Test
    void findCachedMovies_shouldReturnCachedMovies() {
        Movie cachedMovie =
                domainMovie.withLocalPath("/cache/movie.mp4").withStatus(MovieStatus.READY);

        MovieJpaEntity cachedEntity =
                MovieJpaEntity.builder()
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

        List<Movie> result = movieRepositoryAdapter.findCachedMovies();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isCached()).isTrue();
        verify(jpaMovieRepository).findCachedMovies();
        verify(mapper).toDomainList(List.of(cachedEntity));
    }

    @Test
    void getTotalCacheSize_shouldReturnSizeFromRepository() {
        when(jpaMovieRepository.getTotalCacheSize()).thenReturn(5L * 1024 * 1024 * 1024);

        long result = movieRepositoryAdapter.getTotalCacheSize();

        assertThat(result).isEqualTo(5L * 1024 * 1024 * 1024);
        verify(jpaMovieRepository).getTotalCacheSize();
    }

    @Test
    void countCached_shouldReturnCountFromRepository() {
        when(jpaMovieRepository.countByLocalPathIsNotNull()).thenReturn(10L);

        long result = movieRepositoryAdapter.countCached();

        assertThat(result).isEqualTo(10L);
        verify(jpaMovieRepository).countByLocalPathIsNotNull();
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoMovies() {
        when(jpaMovieRepository.findAll()).thenReturn(List.of());
        when(mapper.toDomainList(List.of())).thenReturn(List.of());

        List<Movie> result = movieRepositoryAdapter.findAll();

        assertThat(result).isEmpty();
        verify(jpaMovieRepository).findAll();
    }

    @Test
    void save_shouldHandleNullCategoryCorrectly() {
        Movie movieWithoutCategory = domainMovie.withCategoryId(null);

        MovieJpaEntity entityWithoutCategory =
                MovieJpaEntity.builder()
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

        Movie result = movieRepositoryAdapter.save(movieWithoutCategory);

        assertThat(result.getCategoryId()).isNull();
        verify(mapper).toEntity(movieWithoutCategory);
        verify(jpaMovieRepository).save(any(MovieJpaEntity.class));
    }

    @Test
    void findFavorites_shouldReturnFavoriteMoviesForUser() {
        Movie favoriteMovie = domainMovie.withFavorite(false);
        when(jpaMovieRepository.findFavoritesByUserId("user-1")).thenReturn(List.of(entityMovie));
        when(mapper.toDomainList(List.of(entityMovie))).thenReturn(List.of(favoriteMovie));

        List<Movie> result = movieRepositoryAdapter.findFavorites("user-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isFavorite()).isTrue();
        verify(jpaMovieRepository).findFavoritesByUserId("user-1");
        verify(mapper).toDomainList(List.of(entityMovie));
    }

    @Test
    void addFavorite_shouldPersistFavoriteWhenMissing() {
        when(jpaMovieFavoriteRepository.existsByMovie_IdAndUserId("movie-1", "user-1"))
                .thenReturn(false);
        when(jpaMovieRepository.getReferenceById("movie-1")).thenReturn(entityMovie);

        movieRepositoryAdapter.addFavorite("movie-1", "user-1");

        verify(jpaMovieFavoriteRepository).save(any(MovieFavoriteJpaEntity.class));
    }

    @Test
    void removeFavorite_shouldDeleteFavoriteByUser() {
        movieRepositoryAdapter.removeFavorite("movie-1", "user-1");

        verify(jpaMovieFavoriteRepository).deleteByMovie_IdAndUserId("movie-1", "user-1");
    }

    @Test
    void isFavorite_shouldReturnFavoriteState() {
        when(jpaMovieFavoriteRepository.existsByMovie_IdAndUserId("movie-1", "user-1"))
                .thenReturn(true);

        boolean result = movieRepositoryAdapter.isFavorite("movie-1", "user-1");

        assertThat(result).isTrue();
        verify(jpaMovieFavoriteRepository).existsByMovie_IdAndUserId("movie-1", "user-1");
    }
}
