package com.mediaserver.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.mediaserver.domain.model.Movie;
import com.mediaserver.domain.model.MovieStatus;
import com.mediaserver.domain.repository.MovieRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration tests for repository layer using Testcontainers. These tests verify the actual
 * database operations with PostgreSQL. Requires Docker to be available - tests are skipped if
 * Docker is not present.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class RepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private MovieRepository movieRepository;

    @Test
    void shouldSaveAndRetrieveMovie() {
        // Given
        Movie movie =
                Movie.builder()
                        .title("Integration Test Movie")
                        .description("A movie for integration testing")
                        .year(2024)
                        .megaPath("/test/path")
                        .status(MovieStatus.PENDING)
                        .build();

        // When
        Movie savedMovie = movieRepository.save(movie);

        // Then
        assertThat(savedMovie.getId()).isNotNull();
        assertThat(savedMovie.getTitle()).isEqualTo("Integration Test Movie");

        // Verify it can be retrieved
        Optional<Movie> retrieved = movieRepository.findById(savedMovie.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getTitle()).isEqualTo("Integration Test Movie");
    }

    @Test
    void shouldFindMoviesByStatus() {
        // Given
        Movie pendingMovie =
                Movie.builder()
                        .title("Pending Movie")
                        .megaPath("/path/pending")
                        .status(MovieStatus.PENDING)
                        .build();

        Movie readyMovie =
                Movie.builder()
                        .title("Ready Movie")
                        .megaPath("/path/ready")
                        .localPath("/cache/ready.mp4")
                        .status(MovieStatus.READY)
                        .build();

        movieRepository.save(pendingMovie);
        movieRepository.save(readyMovie);

        // When
        List<Movie> readyMovies = movieRepository.findByStatus(MovieStatus.READY);

        // Then
        assertThat(readyMovies).hasSize(1);
        assertThat(readyMovies.get(0).getTitle()).isEqualTo("Ready Movie");
    }

    @Test
    void shouldFindCachedMovies() {
        // Given
        Movie cachedMovie =
                Movie.builder()
                        .title("Cached Movie")
                        .megaPath("/path/cached")
                        .localPath("/cache/movie.mp4")
                        .status(MovieStatus.READY)
                        .build();

        Movie uncachedMovie =
                Movie.builder()
                        .title("Uncached Movie")
                        .megaPath("/path/uncached")
                        .status(MovieStatus.PENDING)
                        .build();

        movieRepository.save(cachedMovie);
        movieRepository.save(uncachedMovie);

        // When
        List<Movie> cachedMovies = movieRepository.findCachedMovies();

        // Then
        assertThat(cachedMovies).hasSize(1);
        assertThat(cachedMovies.get(0).getTitle()).isEqualTo("Cached Movie");
    }

    @Test
    void shouldFindFavoriteMovies() {
        String userId = "user-1";
        Movie favoriteMovie =
                Movie.builder()
                        .title("Favorite Movie")
                        .megaPath("/path/favorite")
                        .status(MovieStatus.PENDING)
                        .build();

        Movie regularMovie =
                Movie.builder()
                        .title("Regular Movie")
                        .megaPath("/path/regular")
                        .status(MovieStatus.PENDING)
                        .build();

        movieRepository.save(favoriteMovie);
        movieRepository.save(regularMovie);
        movieRepository.addFavorite(favoriteMovie.getId(), userId);
        movieRepository.addFavorite(regularMovie.getId(), "user-2");

        List<Movie> favorites = movieRepository.findFavorites(userId);

        assertThat(favorites).hasSize(1);
        assertThat(favorites.get(0).getTitle()).isEqualTo("Favorite Movie");
    }

    @Test
    void shouldCalculateTotalCacheSize() {
        // Given
        Movie movie1 =
                Movie.builder()
                        .title("Movie 1")
                        .megaPath("/path/1")
                        .localPath("/cache/1.mp4")
                        .fileSize(1000L)
                        .status(MovieStatus.READY)
                        .build();

        Movie movie2 =
                Movie.builder()
                        .title("Movie 2")
                        .megaPath("/path/2")
                        .localPath("/cache/2.mp4")
                        .fileSize(2000L)
                        .status(MovieStatus.READY)
                        .build();

        movieRepository.save(movie1);
        movieRepository.save(movie2);

        // When
        Long totalSize = movieRepository.getTotalCacheSize();

        // Then
        assertThat(totalSize).isEqualTo(3000L);
    }

    @Test
    void shouldSearchMoviesByTitle() {
        // Given
        Movie actionMovie =
                Movie.builder()
                        .title("Action Hero")
                        .megaPath("/path/action")
                        .status(MovieStatus.PENDING)
                        .build();

        Movie comedyMovie =
                Movie.builder()
                        .title("Comedy Night")
                        .megaPath("/path/comedy")
                        .status(MovieStatus.PENDING)
                        .build();

        movieRepository.save(actionMovie);
        movieRepository.save(comedyMovie);

        // When
        List<Movie> searchResults = movieRepository.search("action");

        // Then
        assertThat(searchResults).hasSize(1);
        assertThat(searchResults.get(0).getTitle()).isEqualTo("Action Hero");
    }
}
