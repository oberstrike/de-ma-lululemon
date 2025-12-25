package com.mediaserver.infrastructure.rest.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.mediaserver.application.command.CreateCategoryCommand;
import com.mediaserver.application.command.UpdateCategoryCommand;
import com.mediaserver.domain.model.Category;
import com.mediaserver.infrastructure.rest.dto.CategoryRequestDto;
import com.mediaserver.infrastructure.rest.dto.CategoryResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

/**
 * Unit tests for CategoryRestMapper. Tests conversion between DTOs, domain models, and commands for
 * REST API.
 */
class CategoryRestMapperTest {

    private final CategoryRestMapper mapper = Mappers.getMapper(CategoryRestMapper.class);

    private Category category;
    private CategoryRequestDto requestDto;

    @BeforeEach
    void setUp() {
        category =
                Category.builder()
                        .id("cat-1")
                        .name("Action")
                        .description("Action movies")
                        .megaPath("/Action")
                        .sortOrder(1)
                        .build();

        requestDto =
                CategoryRequestDto.builder()
                        .name("Comedy")
                        .description("Comedy movies")
                        .sortOrder(3)
                        .build();
    }

    @Test
    void toResponse_shouldMapAllFields() {
        // When
        CategoryResponseDto result = mapper.toResponse(category);

        // Then
        assertThat(result.getId()).isEqualTo("cat-1");
        assertThat(result.getName()).isEqualTo("Action");
        assertThat(result.getDescription()).isEqualTo("Action movies");
        assertThat(result.getSortOrder()).isEqualTo(1);
        assertThat(result.getMovieCount()).isEqualTo(0);
    }

    @Test
    void toResponse_shouldReturnZeroMovieCount() {
        // When - domain Category doesn't have movies list, movie count is derived elsewhere
        CategoryResponseDto result = mapper.toResponse(category);

        // Then
        assertThat(result.getMovieCount()).isEqualTo(0);
    }

    @Test
    void toResponse_shouldHandleNullFields() {
        // Given
        Category minimalCategory = Category.builder().id("cat-2").name("Drama").build();

        // When
        CategoryResponseDto result = mapper.toResponse(minimalCategory);

        // Then
        assertThat(result.getId()).isEqualTo("cat-2");
        assertThat(result.getName()).isEqualTo("Drama");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getSortOrder()).isNull();
        assertThat(result.getMovieCount()).isEqualTo(0);
    }

    @Test
    void toResponse_shouldHandleZeroSortOrder() {
        // Given
        Category categoryWithZeroSort = category.withSortOrder(0);

        // When
        CategoryResponseDto result = mapper.toResponse(categoryWithZeroSort);

        // Then
        assertThat(result.getSortOrder()).isEqualTo(0);
    }

    @Test
    void toResponse_shouldHandleNegativeSortOrder() {
        // Given
        Category categoryWithNegativeSort = category.withSortOrder(-1);

        // When
        CategoryResponseDto result = mapper.toResponse(categoryWithNegativeSort);

        // Then
        assertThat(result.getSortOrder()).isEqualTo(-1);
    }

    @Test
    void toResponse_shouldHandleEmptyDescription() {
        // Given
        Category categoryWithEmptyDescription = category.withDescription("");

        // When
        CategoryResponseDto result = mapper.toResponse(categoryWithEmptyDescription);

        // Then
        assertThat(result.getDescription()).isEmpty();
    }

    @Test
    void toCreateCommand_shouldMapAllFields() {
        // When
        CreateCategoryCommand result = mapper.toCreateCommand(requestDto);

        // Then
        assertThat(result.getName()).isEqualTo("Comedy");
        assertThat(result.getDescription()).isEqualTo("Comedy movies");
        assertThat(result.getSortOrder()).isEqualTo(3);
    }

    @Test
    void toCreateCommand_shouldHandleNullOptionalFields() {
        // Given
        CategoryRequestDto minimalRequest = CategoryRequestDto.builder().name("Thriller").build();

        // When
        CreateCategoryCommand result = mapper.toCreateCommand(minimalRequest);

        // Then
        assertThat(result.getName()).isEqualTo("Thriller");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getSortOrder()).isNull();
    }

    @Test
    void toCreateCommand_shouldHandleEmptyDescription() {
        // Given
        CategoryRequestDto requestWithEmptyDescription =
                requestDto.toBuilder().description("").build();

        // When
        CreateCategoryCommand result = mapper.toCreateCommand(requestWithEmptyDescription);

        // Then
        assertThat(result.getDescription()).isEmpty();
    }

    @Test
    void toCreateCommand_shouldHandleSpecialCharactersInName() {
        // Given
        CategoryRequestDto requestWithSpecialChars =
                requestDto.toBuilder().name("Sci-Fi & Fantasy").build();

        // When
        CreateCategoryCommand result = mapper.toCreateCommand(requestWithSpecialChars);

        // Then
        assertThat(result.getName()).isEqualTo("Sci-Fi & Fantasy");
    }

    @Test
    void toCreateCommand_shouldHandleLongDescription() {
        // Given
        String longDescription = "A".repeat(1000);
        CategoryRequestDto requestWithLongDescription =
                requestDto.toBuilder().description(longDescription).build();

        // When
        CreateCategoryCommand result = mapper.toCreateCommand(requestWithLongDescription);

        // Then
        assertThat(result.getDescription()).isEqualTo(longDescription);
    }

    @Test
    void toUpdateCommand_shouldMapAllFieldsIncludingId() {
        // Given
        String categoryId = "cat-123";

        // When
        UpdateCategoryCommand result = mapper.toUpdateCommand(categoryId, requestDto);

        // Then
        assertThat(result.getId()).isEqualTo("cat-123");
        assertThat(result.getName()).isEqualTo("Comedy");
        assertThat(result.getDescription()).isEqualTo("Comedy movies");
        assertThat(result.getSortOrder()).isEqualTo(3);
    }

    @Test
    void toUpdateCommand_shouldHandleNullOptionalFields() {
        // Given
        String categoryId = "cat-456";
        CategoryRequestDto minimalRequest = CategoryRequestDto.builder().name("Horror").build();

        // When
        UpdateCategoryCommand result = mapper.toUpdateCommand(categoryId, minimalRequest);

        // Then
        assertThat(result.getId()).isEqualTo("cat-456");
        assertThat(result.getName()).isEqualTo("Horror");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getSortOrder()).isNull();
    }

    @Test
    void toUpdateCommand_shouldHandleEmptyDescription() {
        // Given
        String categoryId = "cat-789";
        CategoryRequestDto requestWithEmptyDescription =
                requestDto.toBuilder().description("").build();

        // When
        UpdateCategoryCommand result =
                mapper.toUpdateCommand(categoryId, requestWithEmptyDescription);

        // Then
        assertThat(result.getId()).isEqualTo("cat-789");
        assertThat(result.getDescription()).isEmpty();
    }

    @Test
    void toUpdateCommand_shouldHandleZeroSortOrder() {
        // Given
        String categoryId = "cat-000";
        CategoryRequestDto requestWithZeroSort = requestDto.toBuilder().sortOrder(0).build();

        // When
        UpdateCategoryCommand result = mapper.toUpdateCommand(categoryId, requestWithZeroSort);

        // Then
        assertThat(result.getSortOrder()).isEqualTo(0);
    }

    @Test
    void toUpdateCommand_shouldHandleNegativeSortOrder() {
        // Given
        String categoryId = "cat-neg";
        CategoryRequestDto requestWithNegativeSort = requestDto.toBuilder().sortOrder(-1).build();

        // When
        UpdateCategoryCommand result = mapper.toUpdateCommand(categoryId, requestWithNegativeSort);

        // Then
        assertThat(result.getSortOrder()).isEqualTo(-1);
    }

    @Test
    void toUpdateCommand_shouldHandleSpecialCharactersInName() {
        // Given
        String categoryId = "cat-special";
        CategoryRequestDto requestWithSpecialChars =
                requestDto.toBuilder().name("Sci-Fi & Fantasy").build();

        // When
        UpdateCategoryCommand result = mapper.toUpdateCommand(categoryId, requestWithSpecialChars);

        // Then
        assertThat(result.getName()).isEqualTo("Sci-Fi & Fantasy");
    }
}
