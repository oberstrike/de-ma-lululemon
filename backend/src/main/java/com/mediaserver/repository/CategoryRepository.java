package com.mediaserver.repository;

import com.mediaserver.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {

    Optional<Category> findByName(String name);

    Optional<Category> findByMegaPath(String megaPath);

    List<Category> findAllByOrderBySortOrderAsc();

    @Query("SELECT COUNT(m) FROM Movie m WHERE m.category.id = :categoryId")
    long countMoviesByCategoryId(@Param("categoryId") String categoryId);
}
