package com.mediaserver.infrastructure.rest.mapper;

import com.mediaserver.application.command.CreateCategoryCommand;
import com.mediaserver.application.command.UpdateCategoryCommand;
import com.mediaserver.entity.Category;
import com.mediaserver.entity.Movie;
import com.mediaserver.infrastructure.rest.dto.CategoryRequestDto;
import com.mediaserver.infrastructure.rest.dto.CategoryResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CategoryRestMapper.
 * Tests conversion between DTOs, domain models, and commands for REST API.
 */
class CategoryRestMapperTest {

    private final CategoryRestMapper mapper = Mappers.getMapper(CategoryRestMapper.class);

    private Category category;
    private CategoryRequestDto requestDto;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id("cat-1")
                .name("Action")
                .description("Action movies")
                .megaPath("/Action")
                .sortOrder(1)
                .movies(new ArrayList<>())
                .build();

        requestDto = CategoryRequestDto.builder()
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
    void toResponse_shouldCalculateMovieCount() {
        // Given
        List<Movie> movies = new ArrayList<>();
        movies.add(Movie.builder().id("movie-1").title("Movie 1").build());
        movies.add(Movie.builder().id("movie-2").title("Movie 2").build());
        movies.add(Movie.builder().id("movie-3").title("Movie 3").build());

        Category categoryWithMovies = category.toBuilder()
                .movies(movies)
                .build();

        // When
        CategoryResponseDto result = mapper.toResponse(categoryWithMovies);

        // Then
        assertThat(result.getMovieCount()).isEqualTo(3);
    }

    @Test
    void toResponse_shouldHandleNullMoviesList() {
        // Given
        Category categoryWithNullMovies = category.toBuilder()
                .movies(null)
                .build();

        // When
        CategoryResponseDto result = mapper.toResponse(categoryWithNullMovies);

        // Then
        assertThat(result.getMovieCount()).isEqualTo(0);
    }

    @Test
    void toResponse_shouldHandleNullFields() {
        // Given
        Category minimalCategory = Category.builder()
                .id("cat-2")
                .name("Drama")
                .build();

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
        Category categoryWithZeroSort = category.toBuilder()
                .sortOrder(0)
                .build();

        // When
        CategoryResponseDto result = mapper.toResponse(categoryWithZeroSort);

        // Then
        assertThat(result.getSortOrder()).isEqualTo(0);
    }

    @Test
    void toResponse_shouldHandleNegativeSortOrder() {
        // Given
        Category categoryWithNegativeSort = category.toBuilder()
                .sortOrder(-1)
                .build();

        // When
        CategoryResponseDto result = mapper.toResponse(categoryWithNegativeSort);

        // Then
        assertThat(result.getSortOrder()).isEqualTo(-1);
    }

    @Test
    void toResponse_shouldHandleEmptyDescription() {
        // Given
        Category categoryWithEmptyDescription = category.toBuilder()
                .description("")
                .build();

        // When
        CategoryResponseDto result = mapper.toResponse(categoryWithEmptyDescription);

        // Then
        assertThat(result.getDescription()).isEmpty();
    }

    @Test
    void toResponse_shouldHandleLargeMovieCount() {
        // Given
        List<Movie> movies = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            movies.add(Movie.builder().id("movie-" + i).title("Movie " + i).build());
        }

        Category categoryWithManyMovies = category.toBuilder()
                .movies(movies)
                .build();

        // When
        CategoryResponseDto result = mapper.toResponse(categoryWithManyMovies);

        // Then
        assertThat(result.getMovieCount()).isEqualTo(1000);
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
        CategoryRequestDto minimalRequest = CategoryRequestDto.builder()
                .name("Thriller")
                .build();

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
        CategoryRequestDto requestWithEmptyDescription = requestDto.toBuilder()
                .description("")
                .build();

        // When
        CreateCategoryCommand result = mapper.toCreateCommand(requestWithEmptyDescription);

        // Then
        assertThat(result.getDescription()).isEmpty();
    }

    @Test
    void toCreateCommand_shouldHandleSpecialCharactersInName() {
        // Given
        CategoryRequestDto requestWithSpecialChars = requestDto.toBuilder()
                .name("Sci-Fi & Fantasy")
                .build();

        // When
        CreateCategoryCommand result = mapper.toCreateCommand(requestWithSpecialChars);

        // Then
        assertThat(result.getName()).isEqualTo("Sci-Fi & Fantasy");
    }

    @Test
    void toCreateCommand_shouldHandleLongDescription() {
        // Given
        String longDescription = "A".repeat(1000);
        CategoryRequestDto requestWithLongDescription = requestDto.toBuilder()
                .description(longDescription)
                .build();

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
        CategoryRequestDto minimalRequest = CategoryRequestDto.builder()
                .name("Horror")
                .build();

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
        CategoryRequestDto requestWithEmptyDescription = requestDto.toBuilder()
                .description("")
                .build();

        // When
        UpdateCategoryCommand result = mapper.toUpdateCommand(categoryId, requestWithEmptyDescription);

        // Then
        assertThat(result.getId()).isEqualTo("cat-789");
        assertThat(result.getDescription()).isEmpty();
    }

    @Test
    void toUpdateCommand_shouldHandleZeroSortOrder() {
        // Given
        String categoryId = "cat-000";
        CategoryRequestDto requestWithZeroSort = requestDto.toBuilder()
                .sortOrder(0)
                .build();

        // When
        UpdateCategoryCommand result = mapper.toUpdateCommand(categoryId, requestWithZeroSort);

        // Then
        assertThat(result.getSortOrder()).isEqualTo(0);
    }

    @Test
    void toUpdateCommand_shouldHandleNegativeSortOrder() {
        // Given
        String categoryId = "cat-neg";
        CategoryRequestDto requestWithNegativeSort = requestDto.toBuilder()
                .sortOrder(-1)
                .build();

        // When
        UpdateCategoryCommand result = mapper.toUpdateCommand(categoryId, requestWithNegativeSort);

        // Then
        assertThat(result.getSortOrder()).isEqualTo(-1);
    }

    @Test
    void toUpdateCommand_shouldHandleSpecialCharactersInName() {
        // Given
        String categoryId = "cat-special";
        CategoryRequestDto requestWithSpecialChars = requestDto.toBuilder()
                .name("Sci-Fi & Fantasy")
                .build();

        // When
        UpdateCategoryCommand result = mapper.toUpdateCommand(categoryId, requestWithSpecialChars);

        // Then
        assertThat(result.getName()).isEqualTo("Sci-Fi & Fantasy");
    }
}
