package com.mediaserver.infrastructure.persistence.adapter;

import com.mediaserver.domain.model.Category;
import com.mediaserver.domain.repository.CategoryRepository;
import com.mediaserver.infrastructure.persistence.mapper.CategoryPersistenceMapper;
import com.mediaserver.infrastructure.persistence.repository.JpaCategoryRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Adapter implementation of CategoryRepository port.
 * Bridges the domain layer with the JPA persistence layer.
 */
@Repository
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final JpaCategoryRepository jpaCategoryRepository;
    private final CategoryPersistenceMapper mapper;

    @Override
    public Optional<Category> findById(String id) {
        return jpaCategoryRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Category> findAll() {
        return mapper.toDomainList(jpaCategoryRepository.findAll());
    }

    @Override
    public Category save(Category category) {
        var entity = mapper.toEntity(category);

        entity.setId(category.getId());

        var saved = jpaCategoryRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void delete(String id) {
        jpaCategoryRepository.deleteById(id);
    }

    @Override
    public Optional<Category> findByName(String name) {
        return jpaCategoryRepository.findByName(name)
                .map(mapper::toDomain);
    }

    @Override
    public List<Category> findAllOrderBySortOrder() {
        return mapper.toDomainList(jpaCategoryRepository.findAllByOrderBySortOrderAsc());
    }

    @Override
    public long countMoviesByCategoryId(String categoryId) {
        return jpaCategoryRepository.countMoviesByCategoryId(categoryId);
    }

    @Override
    public Optional<Category> findByMegaPath(String megaPath) {
        return jpaCategoryRepository.findByMegaPath(megaPath)
                .map(mapper::toDomain);
    }
}
