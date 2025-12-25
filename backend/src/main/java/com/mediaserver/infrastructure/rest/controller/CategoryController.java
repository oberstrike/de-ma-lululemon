package com.mediaserver.infrastructure.rest.controller;

import com.mediaserver.application.usecase.category.*;
import com.mediaserver.infrastructure.rest.dto.CategoryRequestDto;
import com.mediaserver.infrastructure.rest.dto.CategoryResponseDto;
import com.mediaserver.infrastructure.rest.mapper.CategoryRestMapper;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final GetAllCategoriesUseCase getAllCategoriesUseCase;
    private final GetCategoryUseCase getCategoryUseCase;
    private final CreateCategoryUseCase createCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;
    private final CategoryRestMapper categoryMapper;

    @GetMapping
    public List<CategoryResponseDto> getAllCategories() {
        return getAllCategoriesUseCase.getAllCategories().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public CategoryResponseDto getCategory(@PathVariable String id) {
        return categoryMapper.toResponse(getCategoryUseCase.getCategory(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponseDto createCategory(@Valid @RequestBody CategoryRequestDto request) {
        var category =
                createCategoryUseCase.createCategory(categoryMapper.toCreateCommand(request));
        return categoryMapper.toResponse(category);
    }

    @PutMapping("/{id}")
    public CategoryResponseDto updateCategory(
            @PathVariable String id, @Valid @RequestBody CategoryRequestDto request) {
        var category =
                updateCategoryUseCase.updateCategory(categoryMapper.toUpdateCommand(id, request));
        return categoryMapper.toResponse(category);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable String id) {
        deleteCategoryUseCase.deleteCategory(id);
    }
}
