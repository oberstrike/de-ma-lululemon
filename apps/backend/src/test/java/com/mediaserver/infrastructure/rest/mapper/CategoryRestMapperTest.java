package com.mediaserver.infrastructure.rest.mapper;

import com.mediaserver.application.command.CreateCategoryCommand;
import com.mediaserver.application.command.UpdateCategoryCommand;
import com.mediaserver.domain.model.Category;
import com.mediaserver.infrastructure.rest.dto.CategoryRequestDTO;
import com.mediaserver.infrastructure.rest.dto.CategoryResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CategoryRestMapper. Tests conversion between DTOs, domain models, and commands for
 * REST API.
 */
class CategoryRestMapperTest {

    private final CategoryRestMapper mapper = Mappers.getMapper(CategoryRestMapper.class);

    private Category category;
    private CategoryRequestDTO requestDto;

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
                CategoryRequestDTO.builder()
                        .name("Comedy")
                        .description("Comedy movies")
                        .sortOrder(3)
                        .build();
    }

    @Test
    void toResponse_shouldMapAllFields() {
        CategoryResponseDTO result = mapper.toResponse(category);

        assertThat(result.getId()).isEqualTo("cat-1");
        assertThat(result.getName()).isEqualTo("Action");
        assertThat(result.getDescription()).isEqualTo("Action movies");
        assertThat(result.getSortOrder()).isEqualTo(1);
        assertThat(result.getMovieCount()).isEqualTo(0);
    }

    @Test
    void toResponse_shouldReturnZeroMovieCount() {
        CategoryResponseDTO result = mapper.toResponse(category);

        assertThat(result.getMovieCount()).isEqualTo(0);
    }

    @Test
    void toResponse_shouldHandleNullFields() {
        Category minimalCategory = Category.builder().id("cat-2").name("Drama").build();

        CategoryResponseDTO result = mapper.toResponse(minimalCategory);

        assertThat(result.getId()).isEqualTo("cat-2");
        assertThat(result.getName()).isEqualTo("Drama");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getSortOrder()).isNull();
        assertThat(result.getMovieCount()).isEqualTo(0);
    }

    @Test
    void toResponse_shouldHandleZeroSortOrder() {
        Category categoryWithZeroSort = category.withSortOrder(0);

        CategoryResponseDTO result = mapper.toResponse(categoryWithZeroSort);

        assertThat(result.getSortOrder()).isEqualTo(0);
    }

    @Test
    void toResponse_shouldHandleNegativeSortOrder() {
        Category categoryWithNegativeSort = category.withSortOrder(-1);

        CategoryResponseDTO result = mapper.toResponse(categoryWithNegativeSort);

        assertThat(result.getSortOrder()).isEqualTo(-1);
    }

    @Test
    void toResponse_shouldHandleEmptyDescription() {
        Category categoryWithEmptyDescription = category.withDescription("");

        CategoryResponseDTO result = mapper.toResponse(categoryWithEmptyDescription);

        assertThat(result.getDescription()).isEmpty();
    }

    @Test
    void toCreateCommand_shouldMapAllFields() {
        CreateCategoryCommand result = mapper.toCreateCommand(requestDto);

        assertThat(result.getName()).isEqualTo("Comedy");
        assertThat(result.getDescription()).isEqualTo("Comedy movies");
        assertThat(result.getSortOrder()).isEqualTo(3);
    }

    @Test
    void toCreateCommand_shouldHandleNullOptionalFields() {
        CategoryRequestDTO minimalRequest = CategoryRequestDTO.builder().name("Thriller").build();

        CreateCategoryCommand result = mapper.toCreateCommand(minimalRequest);

        assertThat(result.getName()).isEqualTo("Thriller");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getSortOrder()).isNull();
    }

    @Test
    void toCreateCommand_shouldHandleEmptyDescription() {
        CategoryRequestDTO requestWithEmptyDescription =
                requestDto.toBuilder().description("").build();

        CreateCategoryCommand result = mapper.toCreateCommand(requestWithEmptyDescription);

        assertThat(result.getDescription()).isEmpty();
    }

    @Test
    void toCreateCommand_shouldHandleSpecialCharactersInName() {
        CategoryRequestDTO requestWithSpecialChars =
                requestDto.toBuilder().name("Sci-Fi & Fantasy").build();

        CreateCategoryCommand result = mapper.toCreateCommand(requestWithSpecialChars);

        assertThat(result.getName()).isEqualTo("Sci-Fi & Fantasy");
    }

    @Test
    void toCreateCommand_shouldHandleLongDescription() {
        String longDescription = "A".repeat(1000);
        CategoryRequestDTO requestWithLongDescription =
                requestDto.toBuilder().description(longDescription).build();

        CreateCategoryCommand result = mapper.toCreateCommand(requestWithLongDescription);

        assertThat(result.getDescription()).isEqualTo(longDescription);
    }

    @Test
    void toUpdateCommand_shouldMapAllFieldsIncludingId() {
        String categoryId = "cat-123";

        UpdateCategoryCommand result = mapper.toUpdateCommand(categoryId, requestDto);

        assertThat(result.getId()).isEqualTo("cat-123");
        assertThat(result.getName()).isEqualTo("Comedy");
        assertThat(result.getDescription()).isEqualTo("Comedy movies");
        assertThat(result.getSortOrder()).isEqualTo(3);
    }

    @Test
    void toUpdateCommand_shouldHandleNullOptionalFields() {
        String categoryId = "cat-456";
        CategoryRequestDTO minimalRequest = CategoryRequestDTO.builder().name("Horror").build();

        UpdateCategoryCommand result = mapper.toUpdateCommand(categoryId, minimalRequest);

        assertThat(result.getId()).isEqualTo("cat-456");
        assertThat(result.getName()).isEqualTo("Horror");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getSortOrder()).isNull();
    }

    @Test
    void toUpdateCommand_shouldHandleEmptyDescription() {
        String categoryId = "cat-789";
        CategoryRequestDTO requestWithEmptyDescription =
                requestDto.toBuilder().description("").build();

        UpdateCategoryCommand result =
                mapper.toUpdateCommand(categoryId, requestWithEmptyDescription);

        assertThat(result.getId()).isEqualTo("cat-789");
        assertThat(result.getDescription()).isEmpty();
    }

    @Test
    void toUpdateCommand_shouldHandleZeroSortOrder() {
        String categoryId = "cat-000";
        CategoryRequestDTO requestWithZeroSort = requestDto.toBuilder().sortOrder(0).build();

        UpdateCategoryCommand result = mapper.toUpdateCommand(categoryId, requestWithZeroSort);

        assertThat(result.getSortOrder()).isEqualTo(0);
    }

    @Test
    void toUpdateCommand_shouldHandleNegativeSortOrder() {
        String categoryId = "cat-neg";
        CategoryRequestDTO requestWithNegativeSort = requestDto.toBuilder().sortOrder(-1).build();

        UpdateCategoryCommand result = mapper.toUpdateCommand(categoryId, requestWithNegativeSort);

        assertThat(result.getSortOrder()).isEqualTo(-1);
    }

    @Test
    void toUpdateCommand_shouldHandleSpecialCharactersInName() {
        String categoryId = "cat-special";
        CategoryRequestDTO requestWithSpecialChars =
                requestDto.toBuilder().name("Sci-Fi & Fantasy").build();

        UpdateCategoryCommand result = mapper.toUpdateCommand(categoryId, requestWithSpecialChars);

        assertThat(result.getName()).isEqualTo("Sci-Fi & Fantasy");
    }
}
