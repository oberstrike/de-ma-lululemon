package com.mediaserver.application.port.in;

/**
 * Use case for deleting a category.
 * Defines the input port for category deletion.
 */
public interface DeleteCategoryUseCase {

    /**
     * Deletes a category.
     * @param id the category ID
     * @throws com.mediaserver.exception.CategoryNotFoundException if category not found
     */
    void deleteCategory(String id);
}
