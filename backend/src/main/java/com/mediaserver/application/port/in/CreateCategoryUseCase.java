package com.mediaserver.application.port.in;

import com.mediaserver.application.command.CreateCategoryCommand;
import com.mediaserver.domain.model.Category;

/**
 * Use case for creating a new category.
 * Defines the input port for category creation.
 */
public interface CreateCategoryUseCase {

    /**
     * Creates a new category with the provided data.
     * @param command the category creation command
     * @return the created category
     */
    Category createCategory(CreateCategoryCommand command);
}
