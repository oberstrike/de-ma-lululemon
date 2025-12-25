package com.mediaserver.infrastructure.rest.controller;

import com.mediaserver.application.usecase.category.*;
import com.mediaserver.entity.Category;
import com.mediaserver.infrastructure.rest.dto.CategoryRequestDto;
import com.mediaserver.infrastructure.rest.dto.CategoryResponseDto;
import com.mediaserver.infrastructure.rest.mapper.CategoryRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        var categories = getAllCategoriesUseCase.getAllCategories();
        var response = categories.stream()
                .map(categoryMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> getCategory(@PathVariable String id) {
        var category = getCategoryUseCase.getCategory(id);
        return ResponseEntity.ok(categoryMapper.toResponse(category));
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDto> createCategory(@Valid @RequestBody CategoryRequestDto request) {
        var category = createCategoryUseCase.createCategory(categoryMapper.toCreateCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryMapper.toResponse(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> updateCategory(
            @PathVariable String id,
            @Valid @RequestBody CategoryRequestDto request) {
        var category = updateCategoryUseCase.updateCategory(categoryMapper.toUpdateCommand(id, request));
        return ResponseEntity.ok(categoryMapper.toResponse(category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String id) {
        deleteCategoryUseCase.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
