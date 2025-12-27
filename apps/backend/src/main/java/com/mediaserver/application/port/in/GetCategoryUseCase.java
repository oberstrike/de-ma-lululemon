package com.mediaserver.application.port.in;

import com.mediaserver.domain.model.Category;

import java.util.List;

/** Use case for retrieving category information. Defines the input port for category queries. */
public interface GetCategoryUseCase {

    /**
     * Retrieves a single category by its ID.
     *
     * @param id the category ID
     * @return the category
     * @throws com.mediaserver.exception.CategoryNotFoundException if category not found
     */
    Category getCategory(String id);

    /**
     * Retrieves all categories ordered by sort order.
     *
     * @return list of all categories
     */
    List<Category> getAllCategories();
}
