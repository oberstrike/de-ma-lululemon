package com.mediaserver.application.service;

import com.mediaserver.application.command.CreateCategoryCommand;
import com.mediaserver.application.command.UpdateCategoryCommand;
import com.mediaserver.application.port.in.CreateCategoryUseCase;
import com.mediaserver.application.port.in.DeleteCategoryUseCase;
import com.mediaserver.application.port.in.GetCategoryUseCase;
import com.mediaserver.application.port.in.UpdateCategoryUseCase;
import com.mediaserver.application.port.out.CategoryPort;
import com.mediaserver.domain.model.Category;
import com.mediaserver.exception.CategoryNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service implementing category-related use cases.
 * This service orchestrates the business logic and delegates to output ports.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryApplicationService implements
        GetCategoryUseCase,
        CreateCategoryUseCase,
        UpdateCategoryUseCase,
        DeleteCategoryUseCase {

    private final CategoryPort categoryPort;

    @Override
    public Category getCategory(String id) {
        return categoryPort.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryPort.findAllOrderedBySortOrder();
    }

    @Override
    public Category createCategory(CreateCategoryCommand command) {
        Category category = Category.builder()
                .name(command.getName())
                .description(command.getDescription())
                .sortOrder(command.getSortOrder())
                .build();

        return categoryPort.save(category);
    }

    @Override
    public Category updateCategory(String id, UpdateCategoryCommand command) {
        Category category = categoryPort.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        Category updatedCategory = category.withName(command.getName())
                .withDescription(command.getDescription())
                .withSortOrder(command.getSortOrder());

        return categoryPort.save(updatedCategory);
    }

    @Override
    public void deleteCategory(String id) {
        Category category = categoryPort.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        categoryPort.delete(category);
    }
}
