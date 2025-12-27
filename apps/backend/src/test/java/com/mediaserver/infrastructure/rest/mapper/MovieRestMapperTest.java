package com.mediaserver.infrastructure.rest.mapper;

import com.mediaserver.application.command.CreateMovieCommand;
import com.mediaserver.application.command.UpdateMovieCommand;
import com.mediaserver.domain.model.Movie;
import com.mediaserver.domain.model.MovieStatus;
import com.mediaserver.infrastructure.rest.dto.MovieRequestDTO;
import com.mediaserver.infrastructure.rest.dto.MovieResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for MovieRestMapper. Tests conversion between Movie entities, DTOs, and Commands for
 * REST API.
 */
class MovieRestMapperTest {

    private MovieRestMapper movieRestMapper;

    private Movie movie;
    private MovieRequestDTO requestDto;

    @BeforeEach
    void setUp() {
        movieRestMapper = Mappers.getMapper(MovieRestMapper.class);

        LocalDateTime now = LocalDateTime.now();

        movie =
                Movie.builder()
                        .id("movie-1")
                        .title("Test Movie")
                        .description("A test movie description")
                        .year(2024)
                        .duration("2h 30m")
                        .megaUrl("https://mega.nz/file/test123")
                        .thumbnailUrl("https://example.com/thumb.jpg")
                        .localPath("/cache/test.mp4")
                        .fileSize(1024L * 1024 * 1024)
                        .status(MovieStatus.READY)
                        .categoryId("cat-1")
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

        requestDto =
                MovieRequestDTO.builder()
                        .title("New Movie")
                        .description("A new movie description")
                        .year(2024)
                        .duration("1h 45m")
                        .megaUrl("https://mega.nz/file/new123")
                        .thumbnailUrl("https://example.com/new-thumb.jpg")
                        .categoryId("cat-2")
                        .build();
    }

    @Test
    void toResponse_shouldMapAllBasicFields() {
        MovieResponseDTO result = movieRestMapper.toResponse(movie);

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
    void toResponse_shouldMapCategoryId() {
        MovieResponseDTO result = movieRestMapper.toResponse(movie);

        assertThat(result.getCategoryId()).isEqualTo("cat-1");
        assertThat(result.getCategoryName()).isNull();
    }

    @Test
    void toResponse_shouldMapCachedField_whenMovieIsCached() {
        Movie cachedMovie = movie.withLocalPath("/cache/video.mp4").withStatus(MovieStatus.READY);

        MovieResponseDTO result = movieRestMapper.toResponse(cachedMovie);

        assertThat(result.isCached()).isTrue();
    }

    @Test
    void toResponse_shouldMapCachedFieldFalse_whenNoLocalPath() {
        Movie notCachedMovie = movie.withLocalPath(null).withStatus(MovieStatus.READY);

        MovieResponseDTO result = movieRestMapper.toResponse(notCachedMovie);

        assertThat(result.isCached()).isFalse();
    }

    @Test
    void toResponse_shouldMapCachedFieldFalse_whenStatusNotReady() {
        Movie downloadingMovie =
                movie.withLocalPath("/cache/video.mp4").withStatus(MovieStatus.DOWNLOADING);

        MovieResponseDTO result = movieRestMapper.toResponse(downloadingMovie);

        assertThat(result.isCached()).isFalse();
    }

    @Test
    void toResponse_shouldHandleNullCategoryId() {
        Movie movieWithoutCategory = movie.withCategoryId(null);

        MovieResponseDTO result = movieRestMapper.toResponse(movieWithoutCategory);

        assertThat(result.getCategoryId()).isNull();
        assertThat(result.getCategoryName()).isNull();
    }

    @Test
    void toResponse_shouldHandleNullOptionalFields() {
        Movie minimalMovie =
                Movie.builder()
                        .id("movie-2")
                        .title("Minimal Movie")
                        .status(MovieStatus.PENDING)
                        .build();

        MovieResponseDTO result = movieRestMapper.toResponse(minimalMovie);

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
        Movie pendingMovie = movie.withStatus(MovieStatus.PENDING).withLocalPath(null);
        assertThat(movieRestMapper.toResponse(pendingMovie).getStatus())
                .isEqualTo(MovieStatus.PENDING);

        Movie downloadingMovie = movie.withStatus(MovieStatus.DOWNLOADING);
        assertThat(movieRestMapper.toResponse(downloadingMovie).getStatus())
                .isEqualTo(MovieStatus.DOWNLOADING);

        Movie readyMovie = movie.withStatus(MovieStatus.READY);
        assertThat(movieRestMapper.toResponse(readyMovie).getStatus()).isEqualTo(MovieStatus.READY);
    }

    @Test
    void toResponse_shouldHandleZeroFileSize() {
        Movie movieWithZeroSize = movie.withFileSize(0L);

        MovieResponseDTO result = movieRestMapper.toResponse(movieWithZeroSize);

        assertThat(result.getFileSize()).isEqualTo(0L);
    }

    @Test
    void toResponse_shouldHandleLargeFileSize() {
        Movie movieWithLargeSize = movie.withFileSize(5L * 1024 * 1024 * 1024);

        MovieResponseDTO result = movieRestMapper.toResponse(movieWithLargeSize);

        assertThat(result.getFileSize()).isEqualTo(5L * 1024 * 1024 * 1024);
    }

    @Test
    void toCreateCommand_shouldMapAllFields() {
        CreateMovieCommand result = movieRestMapper.toCreateCommand(requestDto);

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
        MovieRequestDTO minimalRequest =
                MovieRequestDTO.builder()
                        .title("Minimal Movie")
                        .megaUrl("https://mega.nz/file/minimal")
                        .build();

        CreateMovieCommand result = movieRestMapper.toCreateCommand(minimalRequest);

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
        MovieRequestDTO requestWithEmptyStrings =
                MovieRequestDTO.builder()
                        .title("Test Movie")
                        .description("")
                        .megaUrl("https://mega.nz/file/test")
                        .thumbnailUrl("")
                        .categoryId("")
                        .build();

        CreateMovieCommand result = movieRestMapper.toCreateCommand(requestWithEmptyStrings);

        assertThat(result.getTitle()).isEqualTo("Test Movie");
        assertThat(result.getMegaUrl()).isEqualTo("https://mega.nz/file/test");
        assertThat(result.getDescription()).isEqualTo("");
        assertThat(result.getThumbnailUrl()).isEqualTo("");
        assertThat(result.getCategoryId()).isEqualTo("");
    }

    @Test
    void toUpdateCommand_shouldMapAllFieldsIncludingId() {
        String movieId = "movie-update-1";

        UpdateMovieCommand result = movieRestMapper.toUpdateCommand(movieId, requestDto);

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
        String movieId = "movie-update-2";
        MovieRequestDTO minimalRequest =
                MovieRequestDTO.builder()
                        .title("Updated Title")
                        .megaUrl("https://mega.nz/file/updated")
                        .build();

        UpdateMovieCommand result = movieRestMapper.toUpdateCommand(movieId, minimalRequest);

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
        UpdateMovieCommand result = movieRestMapper.toUpdateCommand(null, requestDto);

        assertThat(result.getId()).isNull();
        assertThat(result.getTitle()).isEqualTo("New Movie");
        assertThat(result.getDescription()).isEqualTo("A new movie description");
    }

    @Test
    void toUpdateCommand_shouldMapDifferentIdsCorrectly() {
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
