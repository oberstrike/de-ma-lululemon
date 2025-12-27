package com.mediaserver.infrastructure.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.mediaserver.domain.model.Movie;
import com.mediaserver.domain.model.MovieStatus;
import com.mediaserver.infrastructure.persistence.entity.CategoryJpaEntity;
import com.mediaserver.infrastructure.persistence.entity.MovieJpaEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

/**
 * Unit tests for MoviePersistenceMapper. Tests bidirectional mapping between domain Movie and JPA
 * entity MovieJpaEntity.
 */
class MoviePersistenceMapperTest {

    private MoviePersistenceMapper moviePersistenceMapper;

    private Movie domainMovie;
    private MovieJpaEntity entityMovie;
    private CategoryJpaEntity category;

    @BeforeEach
    void setUp() {
        moviePersistenceMapper = Mappers.getMapper(MoviePersistenceMapper.class);

        category = CategoryJpaEntity.builder().id("cat-1").name("Action").build();

        LocalDateTime now = LocalDateTime.now();

        domainMovie =
                Movie.builder()
                        .id("movie-1")
                        .title("Test Movie")
                        .description("A test movie")
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

        entityMovie =
                MovieJpaEntity.builder()
                        .id("movie-1")
                        .title("Test Movie")
                        .description("A test movie")
                        .year(2024)
                        .duration("2h 30m")
                        .megaUrl("https://mega.nz/file/test")
                        .megaPath("/Movies/Test")
                        .thumbnailUrl("https://example.com/thumb.jpg")
                        .localPath("/cache/test.mp4")
                        .fileSize(1024L * 1024 * 1024)
                        .contentType("video/mp4")
                        .status(MovieStatus.READY)
                        .category(category)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
    }

    @Test
    void toDomain_shouldMapAllFields() {
        Movie result = moviePersistenceMapper.toDomain(entityMovie);

        assertThat(result.getId()).isEqualTo("movie-1");
        assertThat(result.getTitle()).isEqualTo("Test Movie");
        assertThat(result.getDescription()).isEqualTo("A test movie");
        assertThat(result.getYear()).isEqualTo(2024);
        assertThat(result.getDuration()).isEqualTo("2h 30m");
        assertThat(result.getMegaUrl()).isEqualTo("https://mega.nz/file/test");
        assertThat(result.getMegaPath()).isEqualTo("/Movies/Test");
        assertThat(result.getThumbnailUrl()).isEqualTo("https://example.com/thumb.jpg");
        assertThat(result.getLocalPath()).isEqualTo("/cache/test.mp4");
        assertThat(result.getFileSize()).isEqualTo(1024L * 1024 * 1024);
        assertThat(result.getContentType()).isEqualTo("video/mp4");
        assertThat(result.getStatus()).isEqualTo(MovieStatus.READY);
        assertThat(result.getCategoryId()).isEqualTo("cat-1");
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    void toDomain_shouldExtractCategoryId_whenCategoryExists() {
        Movie result = moviePersistenceMapper.toDomain(entityMovie);

        assertThat(result.getCategoryId()).isEqualTo("cat-1");
    }

    @Test
    void toDomain_shouldHandleNullCategory() {
        entityMovie.setCategory(null);

        Movie result = moviePersistenceMapper.toDomain(entityMovie);

        assertThat(result.getCategoryId()).isNull();
    }

    @Test
    void toDomain_shouldHandleNullFields() {
        MovieJpaEntity minimalEntity =
                MovieJpaEntity.builder()
                        .id("movie-2")
                        .title("Minimal Movie")
                        .status(MovieStatus.PENDING)
                        .build();

        Movie result = moviePersistenceMapper.toDomain(minimalEntity);

        assertThat(result.getId()).isEqualTo("movie-2");
        assertThat(result.getTitle()).isEqualTo("Minimal Movie");
        assertThat(result.getStatus()).isEqualTo(MovieStatus.PENDING);
        assertThat(result.getDescription()).isNull();
        assertThat(result.getYear()).isNull();
        assertThat(result.getDuration()).isNull();
        assertThat(result.getMegaUrl()).isNull();
        assertThat(result.getLocalPath()).isNull();
        assertThat(result.getCategoryId()).isNull();
    }

    @Test
    void toEntity_shouldMapAllFields() {
        MovieJpaEntity result = moviePersistenceMapper.toEntity(domainMovie);

        assertThat(result.getId()).isEqualTo("movie-1");
        assertThat(result.getTitle()).isEqualTo("Test Movie");
        assertThat(result.getDescription()).isEqualTo("A test movie");
        assertThat(result.getYear()).isEqualTo(2024);
        assertThat(result.getDuration()).isEqualTo("2h 30m");
        assertThat(result.getMegaUrl()).isEqualTo("https://mega.nz/file/test");
        assertThat(result.getMegaPath()).isEqualTo("/Movies/Test");
        assertThat(result.getThumbnailUrl()).isEqualTo("https://example.com/thumb.jpg");
        assertThat(result.getLocalPath()).isEqualTo("/cache/test.mp4");
        assertThat(result.getFileSize()).isEqualTo(1024L * 1024 * 1024);
        assertThat(result.getContentType()).isEqualTo("video/mp4");
        assertThat(result.getStatus()).isEqualTo(MovieStatus.READY);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    void toEntity_shouldIgnoreCategory_asPerMapperConfiguration() {
        MovieJpaEntity result = moviePersistenceMapper.toEntity(domainMovie);

        assertThat(result.getCategory()).isNull();
    }

    @Test
    void toEntity_shouldHandleNullCategoryId() {
        Movie movieWithoutCategory = domainMovie.withCategoryId(null);

        MovieJpaEntity result = moviePersistenceMapper.toEntity(movieWithoutCategory);

        assertThat(result.getCategory()).isNull();
    }

    @Test
    void toEntity_shouldHandleNullFields() {
        Movie minimalDomain =
                Movie.builder().title("Minimal Movie").status(MovieStatus.PENDING).build();

        MovieJpaEntity result = moviePersistenceMapper.toEntity(minimalDomain);

        assertThat(result.getTitle()).isEqualTo("Minimal Movie");
        assertThat(result.getStatus()).isEqualTo(MovieStatus.PENDING);
        assertThat(result.getDescription()).isNull();
        assertThat(result.getYear()).isNull();
        assertThat(result.getDuration()).isNull();
        assertThat(result.getMegaUrl()).isNull();
        assertThat(result.getLocalPath()).isNull();
        assertThat(result.getCategory()).isNull();
    }

    @Test
    void bidirectionalMapping_shouldPreserveData() {
        MovieJpaEntity entity = moviePersistenceMapper.toEntity(domainMovie);
        entity.setCategory(category);
        Movie result = moviePersistenceMapper.toDomain(entity);

        assertThat(result.getId()).isEqualTo(domainMovie.getId());
        assertThat(result.getTitle()).isEqualTo(domainMovie.getTitle());
        assertThat(result.getDescription()).isEqualTo(domainMovie.getDescription());
        assertThat(result.getStatus()).isEqualTo(domainMovie.getStatus());
        assertThat(result.getCategoryId()).isEqualTo(domainMovie.getCategoryId());
    }

    @Test
    void toDomain_shouldMapIsCachedLogicCorrectly() {
        MovieJpaEntity cachedEntity =
                MovieJpaEntity.builder()
                        .id("movie-1")
                        .title("Test Movie")
                        .localPath("/cache/video.mp4")
                        .status(MovieStatus.READY)
                        .category(category)
                        .build();

        Movie result = moviePersistenceMapper.toDomain(cachedEntity);

        assertThat(result.isCached()).isTrue();
    }

    @Test
    void toDomain_shouldMapNotCached_whenNoLocalPath() {
        MovieJpaEntity notCachedEntity =
                MovieJpaEntity.builder()
                        .id("movie-1")
                        .title("Test Movie")
                        .localPath(null)
                        .status(MovieStatus.READY)
                        .category(category)
                        .build();

        Movie result = moviePersistenceMapper.toDomain(notCachedEntity);

        assertThat(result.isCached()).isFalse();
    }

    @Test
    void toDomain_shouldMapNotCached_whenStatusNotReady() {
        MovieJpaEntity downloadingEntity =
                MovieJpaEntity.builder()
                        .id("movie-1")
                        .title("Test Movie")
                        .localPath("/cache/video.mp4")
                        .status(MovieStatus.DOWNLOADING)
                        .category(category)
                        .build();

        Movie result = moviePersistenceMapper.toDomain(downloadingEntity);

        assertThat(result.isCached()).isFalse();
    }
}
