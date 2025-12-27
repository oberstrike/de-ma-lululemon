package com.mediaserver.infrastructure.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.mediaserver.domain.model.Category;
import com.mediaserver.infrastructure.persistence.entity.CategoryJpaEntity;
import java.util.List;
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
        Category result = categoryPersistenceMapper.toDomain(jpaEntity);

        assertThat(result.getId()).isEqualTo("cat-1");
        assertThat(result.getName()).isEqualTo("Action");
        assertThat(result.getDescription()).isEqualTo("Action movies");
        assertThat(result.getMegaPath()).isEqualTo("/Action");
        assertThat(result.getSortOrder()).isEqualTo(1);
    }

    @Test
    void toDomain_shouldHandleNullFields() {
        CategoryJpaEntity minimalEntity =
                CategoryJpaEntity.builder().id("cat-2").name("Drama").build();

        Category result = categoryPersistenceMapper.toDomain(minimalEntity);

        assertThat(result.getId()).isEqualTo("cat-2");
        assertThat(result.getName()).isEqualTo("Drama");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getMegaPath()).isNull();
        assertThat(result.getSortOrder()).isNull();
    }

    @Test
    void toDomain_shouldNotIncludeMoviesList() {
        CategoryJpaEntity categoryWithMovies =
                CategoryJpaEntity.builder().id("cat-1").name("Action").movies(List.of()).build();

        Category result = categoryPersistenceMapper.toDomain(categoryWithMovies);

        assertThat(result.getId()).isEqualTo("cat-1");
        assertThat(result.getName()).isEqualTo("Action");
    }

    @Test
    void toEntity_shouldMapAllFields() {
        CategoryJpaEntity result = categoryPersistenceMapper.toEntity(domainCategory);

        assertThat(result.getId()).isEqualTo("cat-1");
        assertThat(result.getName()).isEqualTo("Action");
        assertThat(result.getDescription()).isEqualTo("Action movies");
        assertThat(result.getMegaPath()).isEqualTo("/Action");
        assertThat(result.getSortOrder()).isEqualTo(1);
    }

    @Test
    void toEntity_shouldHandleNullFields() {
        Category minimalDomain = Category.builder().name("Comedy").build();

        CategoryJpaEntity result = categoryPersistenceMapper.toEntity(minimalDomain);

        assertThat(result.getName()).isEqualTo("Comedy");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getMegaPath()).isNull();
        assertThat(result.getSortOrder()).isNull();
    }

    @Test
    void toEntity_shouldNotInitializeMoviesList() {
        CategoryJpaEntity result = categoryPersistenceMapper.toEntity(domainCategory);

        assertThat(result.getMovies()).isNotNull();
        assertThat(result.getMovies()).isEmpty();
    }

    @Test
    void bidirectionalMapping_shouldPreserveData() {
        CategoryJpaEntity entity = categoryPersistenceMapper.toEntity(domainCategory);
        Category result = categoryPersistenceMapper.toDomain(entity);

        assertThat(result.getId()).isEqualTo(domainCategory.getId());
        assertThat(result.getName()).isEqualTo(domainCategory.getName());
        assertThat(result.getDescription()).isEqualTo(domainCategory.getDescription());
        assertThat(result.getMegaPath()).isEqualTo(domainCategory.getMegaPath());
        assertThat(result.getSortOrder()).isEqualTo(domainCategory.getSortOrder());
    }

    @Test
    void toDomain_shouldHandleZeroSortOrder() {
        CategoryJpaEntity entityWithZeroSort =
                CategoryJpaEntity.builder()
                        .id(jpaEntity.getId())
                        .name(jpaEntity.getName())
                        .description(jpaEntity.getDescription())
                        .megaPath(jpaEntity.getMegaPath())
                        .sortOrder(0)
                        .build();

        Category result = categoryPersistenceMapper.toDomain(entityWithZeroSort);

        assertThat(result.getSortOrder()).isEqualTo(0);
    }

    @Test
    void toEntity_shouldHandleZeroSortOrder() {
        Category domainWithZeroSort = domainCategory.withSortOrder(0);

        CategoryJpaEntity result = categoryPersistenceMapper.toEntity(domainWithZeroSort);

        assertThat(result.getSortOrder()).isEqualTo(0);
    }

    @Test
    void toDomain_shouldHandleNegativeSortOrder() {
        CategoryJpaEntity entityWithNegativeSort =
                CategoryJpaEntity.builder()
                        .id(jpaEntity.getId())
                        .name(jpaEntity.getName())
                        .description(jpaEntity.getDescription())
                        .megaPath(jpaEntity.getMegaPath())
                        .sortOrder(-1)
                        .build();

        Category result = categoryPersistenceMapper.toDomain(entityWithNegativeSort);

        assertThat(result.getSortOrder()).isEqualTo(-1);
    }

    @Test
    void toDomain_shouldHandleEmptyDescription() {
        CategoryJpaEntity entityWithEmptyDescription =
                CategoryJpaEntity.builder()
                        .id(jpaEntity.getId())
                        .name(jpaEntity.getName())
                        .description("")
                        .megaPath(jpaEntity.getMegaPath())
                        .sortOrder(jpaEntity.getSortOrder())
                        .build();

        Category result = categoryPersistenceMapper.toDomain(entityWithEmptyDescription);

        assertThat(result.getDescription()).isEmpty();
    }

    @Test
    void toEntity_shouldHandleEmptyDescription() {
        Category domainWithEmptyDescription = domainCategory.withDescription("");

        CategoryJpaEntity result = categoryPersistenceMapper.toEntity(domainWithEmptyDescription);

        assertThat(result.getDescription()).isEmpty();
    }

    @Test
    void toDomain_shouldHandleLongDescription() {
        String longDescription = "A".repeat(1000);
        CategoryJpaEntity entityWithLongDescription =
                CategoryJpaEntity.builder()
                        .id(jpaEntity.getId())
                        .name(jpaEntity.getName())
                        .description(longDescription)
                        .megaPath(jpaEntity.getMegaPath())
                        .sortOrder(jpaEntity.getSortOrder())
                        .build();

        Category result = categoryPersistenceMapper.toDomain(entityWithLongDescription);

        assertThat(result.getDescription()).isEqualTo(longDescription);
    }
}
