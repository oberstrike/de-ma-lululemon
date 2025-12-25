package com.mediaserver.service;

import com.mediaserver.dto.CategoryCreateRequest;
import com.mediaserver.dto.CategoryDto;
import com.mediaserver.adapter.CategoryAdapter;
import com.mediaserver.entity.Category;
import com.mediaserver.exception.CategoryNotFoundException;
import com.mediaserver.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryAdapter categoryAdapter;

    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAllByOrderBySortOrderAsc().stream()
                .map(categoryAdapter::toDto)
                .toList();
    }

    public CategoryDto getCategory(String id) {
        return categoryRepository.findById(id)
                .map(categoryAdapter::toDto)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    public CategoryDto createCategory(CategoryCreateRequest request) {
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .sortOrder(request.getSortOrder())
                .build();

        category = categoryRepository.save(category);
        return categoryAdapter.toDto(category);
    }

    public CategoryDto updateCategory(String id, CategoryCreateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder());

        category = categoryRepository.save(category);
        return categoryAdapter.toDto(category);
    }

    public void deleteCategory(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        categoryRepository.delete(category);
    }
}
