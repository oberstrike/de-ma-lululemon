package com.mediaserver.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Category domain entity.
 * Tests domain business logic without mocking - pure unit tests.
 */
class CategoryTest {

    @Test
    void builder_shouldCreateCategoryWithAllFields() {
        // Given & When
        Category category = Category.builder()
                .id("cat-1")
                .name("Action")
                .description("Action movies")
                .megaPath("/Action")
                .sortOrder(1)
                .build();

        // Then
        assertThat(category.getId()).isEqualTo("cat-1");
        assertThat(category.getName()).isEqualTo("Action");
        assertThat(category.getDescription()).isEqualTo("Action movies");
        assertThat(category.getMegaPath()).isEqualTo("/Action");
        assertThat(category.getSortOrder()).isEqualTo(1);
    }

    @Test
    void builder_shouldCreateCategoryWithMinimalFields() {
        // Given & When
        Category category = Category.builder()
                .id("cat-1")
                .name("Drama")
                .build();

        // Then
        assertThat(category.getId()).isEqualTo("cat-1");
        assertThat(category.getName()).isEqualTo("Drama");
        assertThat(category.getDescription()).isNull();
        assertThat(category.getMegaPath()).isNull();
        assertThat(category.getSortOrder()).isNull();
    }

    @Test
    void builder_shouldSupportModification() {
        // Given
        Category original = Category.builder()
                .id("cat-1")
                .name("Original Name")
                .sortOrder(5)
                .build();

        // When - using @With methods
        Category modified = original
                .withName("Modified Name")
                .withDescription("New description")
                .withSortOrder(10);

        // Then
        assertThat(modified.getId()).isEqualTo("cat-1");
        assertThat(modified.getName()).isEqualTo("Modified Name");
        assertThat(modified.getDescription()).isEqualTo("New description");
        assertThat(modified.getSortOrder()).isEqualTo(10);
    }

    @Test
    void equals_shouldReturnTrue_forSameId() {
        // Given
        Category category1 = Category.builder()
                .id("cat-1")
                .name("Category A")
                .build();

        Category category2 = Category.builder()
                .id("cat-1")
                .name("Category A")
                .build();

        // Then - @Value generates equals based on all fields
        assertThat(category1).isEqualTo(category2);
    }

    @Test
    void hashCode_shouldBeSame_forIdenticalObjects() {
        // Given - @Value generates hashCode based on all fields
        Category category1 = Category.builder()
                .id("cat-1")
                .name("Category A")
                .build();

        Category category2 = Category.builder()
                .id("cat-1")
                .name("Category A")
                .build();

        // Then
        assertThat(category1.hashCode()).isEqualTo(category2.hashCode());
    }

    @Test
    void toString_shouldNotThrowException() {
        // Given
        Category category = Category.builder()
                .id("cat-1")
                .name("Test Category")
                .description("Test description")
                .build();

        // When & Then
        assertThat(category.toString()).isNotNull();
        assertThat(category.toString()).contains("Test Category");
    }

    @Test
    void category_shouldBeImmutableThroughBuilder() {
        // Given
        Category category = Category.builder()
                .id("cat-1")
                .name("Immutable Category")
                .sortOrder(1)
                .build();

        // When - attempting to create a copy with changes using @With method
        Category copy = category.withSortOrder(2);

        // Then - original should remain unchanged
        assertThat(category.getSortOrder()).isEqualTo(1);
        assertThat(copy.getSortOrder()).isEqualTo(2);
    }
}
