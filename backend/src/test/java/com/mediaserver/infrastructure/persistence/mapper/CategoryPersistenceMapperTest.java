package com.mediaserver.infrastructure.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.mediaserver.domain.model.Category;
import com.mediaserver.infrastructure.persistence.entity.CategoryJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

/**
 * Unit tests for CategoryPersistenceMapper. Tests bidirectional mapping between domain Category and
 * JPA entity CategoryJpaEntity.
 */
class CategoryPersistenceMapperTest {

    private final CategoryPersistenceMapper categoryPersistenceMapper =
            Mappers.getMapper(CategoryPersistenceMapper.class);

    private Category domainCategory;
    private CategoryJpaEntity jpaEntity;

    @BeforeEach
    void setUp() {
        domainCategory =
                Category.builder()
                        .id("cat-1")
                        .name("Action")
                        .description("Action movies")
                        .megaPath("/Action")
                        .sortOrder(1)
                        .build();

        jpaEntity =
                CategoryJpaEntity.builder()
                        .id("cat-1")
                        .name("Action")
                        .description("Action movies")
                        .megaPath("/Action")
                        .sortOrder(1)
                        .build();
    }

    @Test
    void toDomain_shouldMapAllFields() {
        // When
        Category result = categoryPersistenceMapper.toDomain(jpaEntity);

        // Then
        assertThat(result.getId()).isEqualTo("cat-1");
        assertThat(result.getName()).isEqualTo("Action");
        assertThat(result.getDescription()).isEqualTo("Action movies");
        assertThat(result.getMegaPath()).isEqualTo("/Action");
        assertThat(result.getSortOrder()).isEqualTo(1);
    }

    @Test
    void toDomain_shouldHandleNullFields() {
        // Given
        CategoryJpaEntity minimalEntity =
                CategoryJpaEntity.builder().id("cat-2").name("Drama").build();

        // When
        Category result = categoryPersistenceMapper.toDomain(minimalEntity);

        // Then
        assertThat(result.getId()).isEqualTo("cat-2");
        assertThat(result.getName()).isEqualTo("Drama");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getMegaPath()).isNull();
        assertThat(result.getSortOrder()).isNull();
    }

    @Test
    void toDomain_shouldNotIncludeMoviesList() {
        // Given - entity with movies (lazy-loaded collection)
        CategoryJpaEntity categoryWithMovies =
                CategoryJpaEntity.builder()
                        .id("cat-1")
                        .name("Action")
                        .movies(java.util.List.of()) // movies collection exists
                        .build();

        // When
        Category result = categoryPersistenceMapper.toDomain(categoryWithMovies);

        // Then - domain model should not have movies list
        assertThat(result.getId()).isEqualTo("cat-1");
        assertThat(result.getName()).isEqualTo("Action");
        // Domain Category doesn't have a movies field - it's a pure domain model
    }

    @Test
    void toEntity_shouldMapAllFields() {
        // When
        CategoryJpaEntity result = categoryPersistenceMapper.toEntity(domainCategory);

        // Then
        assertThat(result.getId()).isEqualTo("cat-1");
        assertThat(result.getName()).isEqualTo("Action");
        assertThat(result.getDescription()).isEqualTo("Action movies");
        assertThat(result.getMegaPath()).isEqualTo("/Action");
        assertThat(result.getSortOrder()).isEqualTo(1);
    }

    @Test
    void toEntity_shouldHandleNullFields() {
        // Given
        Category minimalDomain = Category.builder().name("Comedy").build();

        // When
        CategoryJpaEntity result = categoryPersistenceMapper.toEntity(minimalDomain);

        // Then
        assertThat(result.getName()).isEqualTo("Comedy");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getMegaPath()).isNull();
        assertThat(result.getSortOrder()).isNull();
    }

    @Test
    void toEntity_shouldNotInitializeMoviesList() {
        // When
        CategoryJpaEntity result = categoryPersistenceMapper.toEntity(domainCategory);

        // Then - movies list should be initialized by JPA entity's @Builder.Default
        // The mapper shouldn't touch it
        assertThat(result.getMovies()).isNotNull(); // Due to @Builder.Default
        assertThat(result.getMovies()).isEmpty();
    }

    @Test
    void bidirectionalMapping_shouldPreserveData() {
        // When - domain to entity to domain
        CategoryJpaEntity entity = categoryPersistenceMapper.toEntity(domainCategory);
        Category result = categoryPersistenceMapper.toDomain(entity);

        // Then
        assertThat(result.getId()).isEqualTo(domainCategory.getId());
        assertThat(result.getName()).isEqualTo(domainCategory.getName());
        assertThat(result.getDescription()).isEqualTo(domainCategory.getDescription());
        assertThat(result.getMegaPath()).isEqualTo(domainCategory.getMegaPath());
        assertThat(result.getSortOrder()).isEqualTo(domainCategory.getSortOrder());
    }

    @Test
    void toDomain_shouldHandleZeroSortOrder() {
        // Given
        CategoryJpaEntity entityWithZeroSort =
                CategoryJpaEntity.builder()
                        .id(jpaEntity.getId())
                        .name(jpaEntity.getName())
                        .description(jpaEntity.getDescription())
                        .megaPath(jpaEntity.getMegaPath())
                        .sortOrder(0)
                        .build();

        // When
        Category result = categoryPersistenceMapper.toDomain(entityWithZeroSort);

        // Then
        assertThat(result.getSortOrder()).isEqualTo(0);
    }

    @Test
    void toEntity_shouldHandleZeroSortOrder() {
        // Given
        Category domainWithZeroSort = domainCategory.withSortOrder(0);

        // When
        CategoryJpaEntity result = categoryPersistenceMapper.toEntity(domainWithZeroSort);

        // Then
        assertThat(result.getSortOrder()).isEqualTo(0);
    }

    @Test
    void toDomain_shouldHandleNegativeSortOrder() {
        // Given
        CategoryJpaEntity entityWithNegativeSort =
                CategoryJpaEntity.builder()
                        .id(jpaEntity.getId())
                        .name(jpaEntity.getName())
                        .description(jpaEntity.getDescription())
                        .megaPath(jpaEntity.getMegaPath())
                        .sortOrder(-1)
                        .build();

        // When
        Category result = categoryPersistenceMapper.toDomain(entityWithNegativeSort);

        // Then
        assertThat(result.getSortOrder()).isEqualTo(-1);
    }

    @Test
    void toDomain_shouldHandleEmptyDescription() {
        // Given
        CategoryJpaEntity entityWithEmptyDescription =
                CategoryJpaEntity.builder()
                        .id(jpaEntity.getId())
                        .name(jpaEntity.getName())
                        .description("")
                        .megaPath(jpaEntity.getMegaPath())
                        .sortOrder(jpaEntity.getSortOrder())
                        .build();

        // When
        Category result = categoryPersistenceMapper.toDomain(entityWithEmptyDescription);

        // Then
        assertThat(result.getDescription()).isEmpty();
    }

    @Test
    void toEntity_shouldHandleEmptyDescription() {
        // Given
        Category domainWithEmptyDescription = domainCategory.withDescription("");

        // When
        CategoryJpaEntity result = categoryPersistenceMapper.toEntity(domainWithEmptyDescription);

        // Then
        assertThat(result.getDescription()).isEmpty();
    }

    @Test
    void toDomain_shouldHandleLongDescription() {
        // Given
        String longDescription = "A".repeat(1000);
        CategoryJpaEntity entityWithLongDescription =
                CategoryJpaEntity.builder()
                        .id(jpaEntity.getId())
                        .name(jpaEntity.getName())
                        .description(longDescription)
                        .megaPath(jpaEntity.getMegaPath())
                        .sortOrder(jpaEntity.getSortOrder())
                        .build();

        // When
        Category result = categoryPersistenceMapper.toDomain(entityWithLongDescription);

        // Then
        assertThat(result.getDescription()).isEqualTo(longDescription);
    }
}
