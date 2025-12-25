package com.mediaserver.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Movie domain entity. Tests domain business logic without mocking - pure unit
 * tests.
 */
class MovieTest {

    @Test
    void isCached_shouldReturnTrue_whenLocalPathExistsAndStatusIsReady() {
        // Given
        Movie movie =
                Movie.builder()
                        .id("movie-1")
                        .title("Test Movie")
                        .localPath("/path/to/video.mp4")
                        .status(MovieStatus.READY)
                        .build();

        // When
        boolean cached = movie.isCached();

        // Then
        assertThat(cached).isTrue();
    }

    @Test
    void isCached_shouldReturnFalse_whenLocalPathIsNull() {
        // Given
        Movie movie =
                Movie.builder()
                        .id("movie-1")
                        .title("Test Movie")
                        .localPath(null)
                        .status(MovieStatus.READY)
                        .build();

        // When
        boolean cached = movie.isCached();

        // Then
        assertThat(cached).isFalse();
    }

    @Test
    void isCached_shouldReturnFalse_whenStatusIsNotReady() {
        // Given
        Movie movie =
                Movie.builder()
                        .id("movie-1")
                        .title("Test Movie")
                        .localPath("/path/to/video.mp4")
                        .status(MovieStatus.PENDING)
                        .build();

        // When
        boolean cached = movie.isCached();

        // Then
        assertThat(cached).isFalse();
    }

    @Test
    void isCached_shouldReturnFalse_whenStatusIsDownloading() {
        // Given
        Movie movie =
                Movie.builder()
                        .id("movie-1")
                        .title("Test Movie")
                        .localPath("/path/to/video.mp4")
                        .status(MovieStatus.DOWNLOADING)
                        .build();

        // When
        boolean cached = movie.isCached();

        // Then
        assertThat(cached).isFalse();
    }

    @Test
    void builder_shouldCreateMovieWithAllFields() {
        // Given & When
        LocalDateTime now = LocalDateTime.now();
        Movie movie =
                Movie.builder()
                        .id("movie-1")
                        .title("Test Movie")
                        .description("A test movie description")
                        .year(2024)
                        .duration("2h 30m")
                        .megaUrl("https://mega.nz/file/test")
                        .megaPath("/Movies/Test")
                        .thumbnailUrl("https://example.com/thumb.jpg")
                        .localPath("/cache/test.mp4")
                        .fileSize(1024L * 1024 * 1024)
                        .contentType("video/mp4")
                        .status(MovieStatus.READY)
                        .categoryId("cat-1")
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

        // Then
        assertThat(movie.getId()).isEqualTo("movie-1");
        assertThat(movie.getTitle()).isEqualTo("Test Movie");
        assertThat(movie.getDescription()).isEqualTo("A test movie description");
        assertThat(movie.getYear()).isEqualTo(2024);
        assertThat(movie.getDuration()).isEqualTo("2h 30m");
        assertThat(movie.getMegaUrl()).isEqualTo("https://mega.nz/file/test");
        assertThat(movie.getMegaPath()).isEqualTo("/Movies/Test");
        assertThat(movie.getThumbnailUrl()).isEqualTo("https://example.com/thumb.jpg");
        assertThat(movie.getLocalPath()).isEqualTo("/cache/test.mp4");
        assertThat(movie.getFileSize()).isEqualTo(1024L * 1024 * 1024);
        assertThat(movie.getContentType()).isEqualTo("video/mp4");
        assertThat(movie.getStatus()).isEqualTo(MovieStatus.READY);
        assertThat(movie.getCategoryId()).isEqualTo("cat-1");
        assertThat(movie.getCreatedAt()).isEqualTo(now);
        assertThat(movie.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void builder_shouldCreateMovieWithMinimalFields() {
        // Given & When
        Movie movie =
                Movie.builder()
                        .id("movie-1")
                        .title("Minimal Movie")
                        .status(MovieStatus.PENDING)
                        .build();

        // Then
        assertThat(movie.getId()).isEqualTo("movie-1");
        assertThat(movie.getTitle()).isEqualTo("Minimal Movie");
        assertThat(movie.getStatus()).isEqualTo(MovieStatus.PENDING);
        assertThat(movie.getDescription()).isNull();
        assertThat(movie.getYear()).isNull();
        assertThat(movie.getDuration()).isNull();
        assertThat(movie.getLocalPath()).isNull();
        assertThat(movie.isCached()).isFalse();
    }

    @Test
    void builder_shouldSupportDefaults() {
        // Given & When
        Movie movie = Movie.builder().title("Movie with defaults").build();

        // Then - builder can create movie with just title
        assertThat(movie.getTitle()).isEqualTo("Movie with defaults");
    }

    @Test
    void builder_shouldSupportModification() {
        // Given
        Movie original =
                Movie.builder()
                        .id("movie-1")
                        .title("Original Title")
                        .status(MovieStatus.PENDING)
                        .build();

        // When - using @With pattern
        Movie modified =
                original.withTitle("Modified Title")
                        .withStatus(MovieStatus.READY)
                        .withLocalPath("/new/path.mp4");

        // Then
        assertThat(modified.getId()).isEqualTo("movie-1");
        assertThat(modified.getTitle()).isEqualTo("Modified Title");
        assertThat(modified.getStatus()).isEqualTo(MovieStatus.READY);
        assertThat(modified.getLocalPath()).isEqualTo("/new/path.mp4");
        assertThat(modified.isCached()).isTrue();
    }

    @Test
    void equals_shouldReturnTrue_forIdenticalObjects() {
        // Given - @Value generates equals based on all fields
        Movie movie1 = Movie.builder().id("movie-1").title("Movie A").build();

        Movie movie2 = Movie.builder().id("movie-1").title("Movie A").build();

        // Then
        assertThat(movie1).isEqualTo(movie2);
    }

    @Test
    void hashCode_shouldBeSame_forIdenticalObjects() {
        // Given - @Value generates hashCode based on all fields
        Movie movie1 = Movie.builder().id("movie-1").title("Movie A").build();

        Movie movie2 = Movie.builder().id("movie-1").title("Movie A").build();

        // Then
        assertThat(movie1.hashCode()).isEqualTo(movie2.hashCode());
    }

    @Test
    void toString_shouldNotThrowException() {
        // Given
        Movie movie = Movie.builder().id("movie-1").title("Test Movie").categoryId("cat-1").build();

        // When & Then
        assertThat(movie.toString()).isNotNull();
        assertThat(movie.toString()).contains("Test Movie");
    }
}
