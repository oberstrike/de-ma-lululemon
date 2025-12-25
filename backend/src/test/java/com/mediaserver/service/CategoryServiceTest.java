package com.mediaserver.service;

import com.mediaserver.dto.CategoryCreateRequest;
import com.mediaserver.dto.CategoryDto;
import com.mediaserver.dto.CategoryMapper;
import com.mediaserver.entity.Category;
import com.mediaserver.exception.CategoryNotFoundException;
import com.mediaserver.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id("cat-1")
                .name("Action")
                .description("Action movies")
                .sortOrder(1)
                .movies(new ArrayList<>())
                .build();
    }

    @Test
    void getAllCategories_shouldReturnAllCategories() {
        stubCategoryMapper();
        when(categoryRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of(testCategory));

        List<CategoryDto> result = categoryService.getAllCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Action");
        verify(categoryRepository).findAllByOrderBySortOrderAsc();
    }

    @Test
    void getCategory_shouldReturnCategory_whenExists() {
        stubCategoryMapper();
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(testCategory));

        CategoryDto result = categoryService.getCategory("cat-1");

        assertThat(result.getId()).isEqualTo("cat-1");
        assertThat(result.getName()).isEqualTo("Action");
    }

    @Test
    void getCategory_shouldThrowException_whenNotFound() {
        when(categoryRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategory("nonexistent"))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("nonexistent");
    }

    @Test
    void createCategory_shouldCreateAndReturnCategory() {
        stubCategoryMapper();
        CategoryCreateRequest request = CategoryCreateRequest.builder()
                .name("Comedy")
                .description("Comedy movies")
                .sortOrder(2)
                .build();

        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId("new-cat-id");
            return c;
        });

        CategoryDto result = categoryService.createCategory(request);

        assertThat(result.getId()).isEqualTo("new-cat-id");
        assertThat(result.getName()).isEqualTo("Comedy");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_shouldUpdateAndReturnCategory() {
        stubCategoryMapper();
        CategoryCreateRequest request = CategoryCreateRequest.builder()
                .name("Updated Action")
                .description("Updated description")
                .sortOrder(5)
                .build();

        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        CategoryDto result = categoryService.updateCategory("cat-1", request);

        assertThat(result.getName()).isEqualTo("Updated Action");
        assertThat(testCategory.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void updateCategory_shouldThrowException_whenNotFound() {
        CategoryCreateRequest request = CategoryCreateRequest.builder()
                .name("Updated")
                .build();

        when(categoryRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory("nonexistent", request))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void deleteCategory_shouldDeleteCategory_whenExists() {
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(testCategory));

        categoryService.deleteCategory("cat-1");

        verify(categoryRepository).delete(testCategory);
    }

    @Test
    void deleteCategory_shouldThrowException_whenNotFound() {
        when(categoryRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory("nonexistent"))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    private CategoryDto toDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .sortOrder(category.getSortOrder())
                .movieCount(0)
                .build();
    }

    private void stubCategoryMapper() {
        when(categoryMapper.toDto(any(Category.class), eq(categoryRepository)))
                .thenAnswer(invocation -> toDto(invocation.getArgument(0)));
    }
}
