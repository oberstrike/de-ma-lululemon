package com.mediaserver.application.port.out;

import com.mediaserver.domain.model.Category;
import java.util.List;
import java.util.Optional;

/**
 * Output port for category persistence operations.
 * This port will be implemented by the persistence adapter.
 */
public interface CategoryPort {

    /**
     * Finds a category by its ID.
     * @param id the category ID
     * @return optional containing the category if found
     */
    Optional<Category> findById(String id);

    /**
     * Finds all categories ordered by sort order.
     * @return list of all categories
     */
    List<Category> findAllOrderedBySortOrder();

    /**
     * Finds a category by name.
     * @param name the category name
     * @return optional containing the category if found
     */
    Optional<Category> findByName(String name);

    /**
     * Counts movies in a category.
     * @param categoryId the category ID
     * @return number of movies in the category
     */
    long countMoviesInCategory(String categoryId);

    /**
     * Saves a category.
     * @param category the category to save
     * @return the saved category
     */
    Category save(Category category);

    /**
     * Deletes a category.
     * @param category the category to delete
     */
    void delete(Category category);
}
