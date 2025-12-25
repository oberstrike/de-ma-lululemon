package com.mediaserver.application.port.in;

import com.mediaserver.application.command.UpdateCategoryCommand;
import com.mediaserver.domain.model.Category;

/**
 * Use case for updating an existing category.
 * Defines the input port for category updates.
 */
public interface UpdateCategoryUseCase {

    /**
     * Updates an existing category with the provided data.
     * @param id the category ID
     * @param command the category update command
     * @return the updated category
     * @throws com.mediaserver.exception.CategoryNotFoundException if category not found
     */
    Category updateCategory(String id, UpdateCategoryCommand command);
}
