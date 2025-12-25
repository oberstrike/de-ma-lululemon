package com.mediaserver.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.mediaserver.application.command.CreateCategoryCommand;
import com.mediaserver.application.command.UpdateCategoryCommand;
import com.mediaserver.application.port.out.CategoryPort;
import com.mediaserver.domain.model.Category;
import com.mediaserver.exception.CategoryNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for CategoryApplicationService.
 * Tests use case orchestration with mocked output ports.
 */
@ExtendWith(MockitoExtension.class)
class CategoryApplicationServiceTest {

    @Mock
    private CategoryPort categoryPort;

    @InjectMocks
    private CategoryApplicationService categoryApplicationService;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id("cat-1")
                .name("Action")
                .description("Action movies")
                .megaPath("/Action")
                .sortOrder(1)
                .build();
    }

    @Test
    void getAllCategories_shouldReturnAllCategoriesSortedByOrder() {
        // Given
        Category category2 = Category.builder()
                .id("cat-2")
                .name("Drama")
                .sortOrder(2)
                .build();

        when(categoryPort.findAllOrderedBySortOrder()).thenReturn(List.of(testCategory, category2));

        // When
        List<Category> result = categoryApplicationService.getAllCategories();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSortOrder()).isEqualTo(1);
        assertThat(result.get(1).getSortOrder()).isEqualTo(2);
        verify(categoryPort).findAllOrderedBySortOrder();
    }

    @Test
    void getCategory_shouldReturnCategory_whenExists() {
        // Given
        when(categoryPort.findById("cat-1")).thenReturn(Optional.of(testCategory));

        // When
        Category result = categoryApplicationService.getCategory("cat-1");

        // Then
        assertThat(result.getId()).isEqualTo("cat-1");
        assertThat(result.getName()).isEqualTo("Action");
        verify(categoryPort).findById("cat-1");
    }

    @Test
    void getCategory_shouldThrowException_whenNotFound() {
        // Given
        when(categoryPort.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryApplicationService.getCategory("nonexistent"))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("nonexistent");
        verify(categoryPort).findById("nonexistent");
    }

    @Test
    void createCategory_shouldSaveAndReturnCategory() {
        // Given
        CreateCategoryCommand command = CreateCategoryCommand.builder()
                .name("Comedy")
                .description("Comedy movies")
                .sortOrder(3)
                .build();

        Category savedCategory = Category.builder()
                .id("new-cat-id")
                .name("Comedy")
                .description("Comedy movies")
                .sortOrder(3)
                .build();

        when(categoryPort.save(any(Category.class))).thenReturn(savedCategory);

        // When
        Category result = categoryApplicationService.createCategory(command);

        // Then
        assertThat(result.getId()).isEqualTo("new-cat-id");
        assertThat(result.getName()).isEqualTo("Comedy");
        assertThat(result.getDescription()).isEqualTo("Comedy movies");
        assertThat(result.getSortOrder()).isEqualTo(3);
        verify(categoryPort).save(any(Category.class));
    }

    @Test
    void updateCategory_shouldUpdateAndReturnCategory() {
        // Given
        UpdateCategoryCommand command = UpdateCategoryCommand.builder()
                .name("Updated Action")
                .description("Updated description")
                .sortOrder(5)
                .build();

        Category updatedCategory = testCategory.withName("Updated Action")
                .withDescription("Updated description")
                .withSortOrder(5);

        when(categoryPort.findById("cat-1")).thenReturn(Optional.of(testCategory));
        when(categoryPort.save(any(Category.class))).thenReturn(updatedCategory);

        // When
        Category result = categoryApplicationService.updateCategory("cat-1", command);

        // Then
        assertThat(result.getName()).isEqualTo("Updated Action");
        assertThat(result.getDescription()).isEqualTo("Updated description");
        assertThat(result.getSortOrder()).isEqualTo(5);
        verify(categoryPort).findById("cat-1");
        verify(categoryPort).save(any(Category.class));
    }

    @Test
    void updateCategory_shouldThrowException_whenNotFound() {
        // Given
        UpdateCategoryCommand command = UpdateCategoryCommand.builder().name("Updated").build();
        when(categoryPort.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryApplicationService.updateCategory("nonexistent", command))
                .isInstanceOf(CategoryNotFoundException.class);
        verify(categoryPort).findById("nonexistent");
        verify(categoryPort, never()).save(any());
    }

    @Test
    void deleteCategory_shouldCallPortDelete_whenExists() {
        // Given
        when(categoryPort.findById("cat-1")).thenReturn(Optional.of(testCategory));
        doNothing().when(categoryPort).delete(testCategory);

        // When
        categoryApplicationService.deleteCategory("cat-1");

        // Then
        verify(categoryPort).findById("cat-1");
        verify(categoryPort).delete(testCategory);
    }

    @Test
    void deleteCategory_shouldThrowException_whenNotFound() {
        // Given
        when(categoryPort.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryApplicationService.deleteCategory("nonexistent"))
                .isInstanceOf(CategoryNotFoundException.class);
        verify(categoryPort).findById("nonexistent");
        verify(categoryPort, never()).delete(any(Category.class));
    }
}
