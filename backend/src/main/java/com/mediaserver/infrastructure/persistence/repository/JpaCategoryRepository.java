package com.mediaserver.infrastructure.persistence.repository;

import com.mediaserver.infrastructure.persistence.entity.CategoryJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for CategoryJpaEntity. This is an infrastructure component that
 * provides persistence operations.
 */
public interface JpaCategoryRepository extends JpaRepository<CategoryJpaEntity, String> {

    Optional<CategoryJpaEntity> findByName(String name);

    Optional<CategoryJpaEntity> findByMegaPath(String megaPath);

    List<CategoryJpaEntity> findAllByOrderBySortOrderAsc();

    @Query("SELECT COUNT(m) FROM MovieJpaEntity m WHERE m.category.id = :categoryId")
    long countMoviesByCategoryId(@Param("categoryId") String categoryId);
}
