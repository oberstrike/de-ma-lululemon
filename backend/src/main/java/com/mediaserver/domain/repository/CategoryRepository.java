package com.mediaserver.domain.repository;

import com.mediaserver.domain.model.Category;

import java.util.List;
import java.util.Optional;

/**
 * Repository port interface for Category domain entity.
 * This is a port in hexagonal architecture - implementations are adapters.
 */
public interface CategoryRepository {

    /**
     * Find a category by its ID.
     * @param id the category ID
     * @return Optional containing the category if found
     */
    Optional<Category> findById(String id);

    /**
     * Find all categories.
     * @return list of all categories
     */
    List<Category> findAll();

    /**
     * Save a category (create or update).
     * @param category the category to save
     * @return the saved category
     */
    Category save(Category category);

    /**
     * Delete a category by its ID.
     * @param id the category ID
     */
    void delete(String id);

    /**
     * Find a category by its name.
     * @param name the category name
     * @return Optional containing the category if found
     */
    Optional<Category> findByName(String name);

    /**
     * Find all categories ordered by sort order ascending.
     * @return list of categories ordered by sort order
     */
    List<Category> findAllOrderBySortOrder();

    /**
     * Count number of movies in a category.
     * @param categoryId the category ID
     * @return number of movies in the category
     */
    long countMoviesByCategoryId(String categoryId);

    /**
     * Find a category by its Mega path.
     * @param megaPath the Mega path
     * @return Optional containing the category if found
     */
    Optional<Category> findByMegaPath(String megaPath);
}
