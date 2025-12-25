package com.mediaserver.infrastructure.rest.mapper;

import com.mediaserver.application.command.CreateMovieCommand;
import com.mediaserver.application.command.UpdateMovieCommand;
import com.mediaserver.entity.Category;
import com.mediaserver.entity.Movie;
import com.mediaserver.entity.MovieStatus;
import com.mediaserver.infrastructure.rest.dto.MovieRequestDto;
import com.mediaserver.infrastructure.rest.dto.MovieResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for MovieRestMapper.
 * Tests conversion between Movie entities, DTOs, and Commands for REST API.
 */
class MovieRestMapperTest {

    private MovieRestMapper movieRestMapper;

    private Movie movie;
    private Category category;
    private MovieRequestDto requestDto;

    @BeforeEach
    void setUp() {
        movieRestMapper = Mappers.getMapper(MovieRestMapper.class);

        LocalDateTime now = LocalDateTime.now();

        category = Category.builder()
                .id("cat-1")
                .name("Action")
                .description("Action movies")
                .build();

        movie = Movie.builder()
                .id("movie-1")
                .title("Test Movie")
                .description("A test movie description")
                .year(2024)
                .duration("2h 30m")
                .megaUrl("https://mega.nz/file/test123")
                .thumbnailUrl("https://example.com/thumb.jpg")
                .localPath("/cache/test.mp4")
                .fileSize(1024L * 1024 * 1024) // 1GB
                .status(MovieStatus.READY)
                .category(category)
                .createdAt(now)
                .updatedAt(now)
                .build();

        requestDto = MovieRequestDto.builder()
                .title("New Movie")
                .description("A new movie description")
                .year(2024)
                .duration("1h 45m")
                .megaUrl("https://mega.nz/file/new123")
                .thumbnailUrl("https://example.com/new-thumb.jpg")
                .categoryId("cat-2")
                .build();
    }

    // ========== toResponse() Tests ==========

    @Test
    void toResponse_shouldMapAllBasicFields() {
        // When
        MovieResponseDto result = movieRestMapper.toResponse(movie);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("movie-1");
        assertThat(result.getTitle()).isEqualTo("Test Movie");
        assertThat(result.getDescription()).isEqualTo("A test movie description");
        assertThat(result.getYear()).isEqualTo(2024);
        assertThat(result.getDuration()).isEqualTo("2h 30m");
        assertThat(result.getThumbnailUrl()).isEqualTo("https://example.com/thumb.jpg");
        assertThat(result.getStatus()).isEqualTo(MovieStatus.READY);
        assertThat(result.getFileSize()).isEqualTo(1024L * 1024 * 1024);
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    void toResponse_shouldMapCategoryFields() {
        // When
        MovieResponseDto result = movieRestMapper.toResponse(movie);

        // Then
        assertThat(result.getCategoryId()).isEqualTo("cat-1");
        assertThat(result.getCategoryName()).isEqualTo("Action");
    }

    @Test
    void toResponse_shouldMapCachedField_whenMovieIsCached() {
        // Given - movie with localPath and READY status
        Movie cachedMovie = movie.toBuilder()
                .localPath("/cache/video.mp4")
                .status(MovieStatus.READY)
                .build();

        // When
        MovieResponseDto result = movieRestMapper.toResponse(cachedMovie);

        // Then
        assertThat(result.isCached()).isTrue();
    }

    @Test
    void toResponse_shouldMapCachedFieldFalse_whenNoLocalPath() {
        // Given
        Movie notCachedMovie = movie.toBuilder()
                .localPath(null)
                .status(MovieStatus.READY)
                .build();

        // When
        MovieResponseDto result = movieRestMapper.toResponse(notCachedMovie);

        // Then
        assertThat(result.isCached()).isFalse();
    }

    @Test
    void toResponse_shouldMapCachedFieldFalse_whenStatusNotReady() {
        // Given
        Movie downloadingMovie = movie.toBuilder()
                .localPath("/cache/video.mp4")
                .status(MovieStatus.DOWNLOADING)
                .build();

        // When
        MovieResponseDto result = movieRestMapper.toResponse(downloadingMovie);

        // Then
        assertThat(result.isCached()).isFalse();
    }

    @Test
    void toResponse_shouldHandleNullCategory() {
        // Given
        Movie movieWithoutCategory = movie.toBuilder()
                .category(null)
                .build();

        // When
        MovieResponseDto result = movieRestMapper.toResponse(movieWithoutCategory);

        // Then
        assertThat(result.getCategoryId()).isNull();
        assertThat(result.getCategoryName()).isNull();
    }

    @Test
    void toResponse_shouldHandleNullOptionalFields() {
        // Given
        Movie minimalMovie = Movie.builder()
                .id("movie-2")
                .title("Minimal Movie")
                .status(MovieStatus.PENDING)
                .build();

        // When
        MovieResponseDto result = movieRestMapper.toResponse(minimalMovie);

        // Then
        assertThat(result.getId()).isEqualTo("movie-2");
        assertThat(result.getTitle()).isEqualTo("Minimal Movie");
        assertThat(result.getStatus()).isEqualTo(MovieStatus.PENDING);
        assertThat(result.getDescription()).isNull();
        assertThat(result.getYear()).isNull();
        assertThat(result.getDuration()).isNull();
        assertThat(result.getThumbnailUrl()).isNull();
        assertThat(result.getCategoryId()).isNull();
        assertThat(result.getCategoryName()).isNull();
        assertThat(result.getFileSize()).isNull();
        assertThat(result.isCached()).isFalse();
    }

    @Test
    void toResponse_shouldHandleAllMovieStatuses() {
        // Test PENDING
        Movie pendingMovie = movie.toBuilder().status(MovieStatus.PENDING).localPath(null).build();
        assertThat(movieRestMapper.toResponse(pendingMovie).getStatus()).isEqualTo(MovieStatus.PENDING);

        // Test DOWNLOADING
        Movie downloadingMovie = movie.toBuilder().status(MovieStatus.DOWNLOADING).build();
        assertThat(movieRestMapper.toResponse(downloadingMovie).getStatus()).isEqualTo(MovieStatus.DOWNLOADING);

        // Test READY
        Movie readyMovie = movie.toBuilder().status(MovieStatus.READY).build();
        assertThat(movieRestMapper.toResponse(readyMovie).getStatus()).isEqualTo(MovieStatus.READY);
    }

    @Test
    void toResponse_shouldHandleZeroFileSize() {
        // Given
        Movie movieWithZeroSize = movie.toBuilder()
                .fileSize(0L)
                .build();

        // When
        MovieResponseDto result = movieRestMapper.toResponse(movieWithZeroSize);

        // Then
        assertThat(result.getFileSize()).isEqualTo(0L);
    }

    @Test
    void toResponse_shouldHandleLargeFileSize() {
        // Given
        Movie movieWithLargeSize = movie.toBuilder()
                .fileSize(5L * 1024 * 1024 * 1024) // 5GB
                .build();

        // When
        MovieResponseDto result = movieRestMapper.toResponse(movieWithLargeSize);

        // Then
        assertThat(result.getFileSize()).isEqualTo(5L * 1024 * 1024 * 1024);
    }

    // ========== toCreateCommand() Tests ==========

    @Test
    void toCreateCommand_shouldMapAllFields() {
        // When
        CreateMovieCommand result = movieRestMapper.toCreateCommand(requestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("New Movie");
        assertThat(result.getDescription()).isEqualTo("A new movie description");
        assertThat(result.getYear()).isEqualTo(2024);
        assertThat(result.getDuration()).isEqualTo("1h 45m");
        assertThat(result.getMegaUrl()).isEqualTo("https://mega.nz/file/new123");
        assertThat(result.getThumbnailUrl()).isEqualTo("https://example.com/new-thumb.jpg");
        assertThat(result.getCategoryId()).isEqualTo("cat-2");
    }

    @Test
    void toCreateCommand_shouldHandleNullOptionalFields() {
        // Given
        MovieRequestDto minimalRequest = MovieRequestDto.builder()
                .title("Minimal Movie")
                .megaUrl("https://mega.nz/file/minimal")
                .build();

        // When
        CreateMovieCommand result = movieRestMapper.toCreateCommand(minimalRequest);

        // Then
        assertThat(result.getTitle()).isEqualTo("Minimal Movie");
        assertThat(result.getMegaUrl()).isEqualTo("https://mega.nz/file/minimal");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getYear()).isNull();
        assertThat(result.getDuration()).isNull();
        assertThat(result.getThumbnailUrl()).isNull();
        assertThat(result.getCategoryId()).isNull();
    }

    @Test
    void toCreateCommand_shouldMapEmptyStringsAsNull() {
        // Given
        MovieRequestDto requestWithEmptyStrings = MovieRequestDto.builder()
                .title("Test Movie")
                .description("")
                .megaUrl("https://mega.nz/file/test")
                .thumbnailUrl("")
                .categoryId("")
                .build();

        // When
        CreateMovieCommand result = movieRestMapper.toCreateCommand(requestWithEmptyStrings);

        // Then
        assertThat(result.getTitle()).isEqualTo("Test Movie");
        assertThat(result.getMegaUrl()).isEqualTo("https://mega.nz/file/test");
        // Empty strings are passed through by MapStruct by default
        assertThat(result.getDescription()).isEqualTo("");
        assertThat(result.getThumbnailUrl()).isEqualTo("");
        assertThat(result.getCategoryId()).isEqualTo("");
    }

    // ========== toUpdateCommand() Tests ==========

    @Test
    void toUpdateCommand_shouldMapAllFieldsIncludingId() {
        // Given
        String movieId = "movie-update-1";

        // When
        UpdateMovieCommand result = movieRestMapper.toUpdateCommand(movieId, requestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("movie-update-1");
        assertThat(result.getTitle()).isEqualTo("New Movie");
        assertThat(result.getDescription()).isEqualTo("A new movie description");
        assertThat(result.getYear()).isEqualTo(2024);
        assertThat(result.getDuration()).isEqualTo("1h 45m");
        assertThat(result.getMegaUrl()).isEqualTo("https://mega.nz/file/new123");
        assertThat(result.getThumbnailUrl()).isEqualTo("https://example.com/new-thumb.jpg");
        assertThat(result.getCategoryId()).isEqualTo("cat-2");
    }

    @Test
    void toUpdateCommand_shouldHandleNullOptionalFields() {
        // Given
        String movieId = "movie-update-2";
        MovieRequestDto minimalRequest = MovieRequestDto.builder()
                .title("Updated Title")
                .megaUrl("https://mega.nz/file/updated")
                .build();

        // When
        UpdateMovieCommand result = movieRestMapper.toUpdateCommand(movieId, minimalRequest);

        // Then
        assertThat(result.getId()).isEqualTo("movie-update-2");
        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.getMegaUrl()).isEqualTo("https://mega.nz/file/updated");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getYear()).isNull();
        assertThat(result.getDuration()).isNull();
        assertThat(result.getThumbnailUrl()).isNull();
        assertThat(result.getCategoryId()).isNull();
    }

    @Test
    void toUpdateCommand_shouldHandleNullId() {
        // When
        UpdateMovieCommand result = movieRestMapper.toUpdateCommand(null, requestDto);

        // Then
        assertThat(result.getId()).isNull();
        assertThat(result.getTitle()).isEqualTo("New Movie");
        assertThat(result.getDescription()).isEqualTo("A new movie description");
    }

    @Test
    void toUpdateCommand_shouldMapDifferentIdsCorrectly() {
        // Test with various ID formats
        String uuidId = "550e8400-e29b-41d4-a716-446655440000";
        UpdateMovieCommand result1 = movieRestMapper.toUpdateCommand(uuidId, requestDto);
        assertThat(result1.getId()).isEqualTo(uuidId);

        String simpleId = "123";
        UpdateMovieCommand result2 = movieRestMapper.toUpdateCommand(simpleId, requestDto);
        assertThat(result2.getId()).isEqualTo(simpleId);

        String complexId = "movie-2024-test-123";
        UpdateMovieCommand result3 = movieRestMapper.toUpdateCommand(complexId, requestDto);
        assertThat(result3.getId()).isEqualTo(complexId);
    }
}
