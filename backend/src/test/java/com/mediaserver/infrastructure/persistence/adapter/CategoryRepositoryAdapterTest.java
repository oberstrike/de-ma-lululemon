package com.mediaserver.infrastructure.persistence.adapter;

import com.mediaserver.domain.model.Category;
import com.mediaserver.infrastructure.persistence.entity.CategoryJpaEntity;
import com.mediaserver.infrastructure.persistence.mapper.CategoryPersistenceMapper;
import com.mediaserver.infrastructure.persistence.repository.JpaCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CategoryRepositoryAdapter.
 * Tests the adapter implementation that bridges domain and persistence layers.
 */
@ExtendWith(MockitoExtension.class)
class CategoryRepositoryAdapterTest {

    @Mock
    private JpaCategoryRepository jpaCategoryRepository;

    @Mock
    private CategoryPersistenceMapper categoryPersistenceMapper;

    @InjectMocks
    private CategoryRepositoryAdapter categoryRepositoryAdapter;

    private Category domainCategory;
    private CategoryJpaEntity entityCategory;

    @BeforeEach
    void setUp() {
        domainCategory = Category.builder()
                .id("cat-1")
                .name("Action")
                .description("Action movies")
                .megaPath("/Action")
                .sortOrder(1)
                .build();

        entityCategory = CategoryJpaEntity.builder()
                .id("cat-1")
                .name("Action")
                .description("Action movies")
                .megaPath("/Action")
                .sortOrder(1)
                .build();
    }

    @Test
    void findAllOrderBySortOrder_shouldMapEntitiesToDomainModels() {
        // Given
        when(jpaCategoryRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of(entityCategory));
        when(categoryPersistenceMapper.toDomainList(List.of(entityCategory))).thenReturn(List.of(domainCategory));

        // When
        List<Category> result = categoryRepositoryAdapter.findAllOrderBySortOrder();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Action");
        assertThat(result.get(0).getSortOrder()).isEqualTo(1);
        verify(jpaCategoryRepository).findAllByOrderBySortOrderAsc();
        verify(categoryPersistenceMapper).toDomainList(List.of(entityCategory));
    }

    @Test
    void findById_shouldReturnDomainModel_whenExists() {
        // Given
        when(jpaCategoryRepository.findById("cat-1")).thenReturn(Optional.of(entityCategory));
        when(categoryPersistenceMapper.toDomain(entityCategory)).thenReturn(domainCategory);

        // When
        Optional<Category> result = categoryRepositoryAdapter.findById("cat-1");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("cat-1");
        assertThat(result.get().getName()).isEqualTo("Action");
        verify(jpaCategoryRepository).findById("cat-1");
        verify(categoryPersistenceMapper).toDomain(entityCategory);
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        // Given
        when(jpaCategoryRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When
        Optional<Category> result = categoryRepositoryAdapter.findById("nonexistent");

        // Then
        assertThat(result).isEmpty();
        verify(jpaCategoryRepository).findById("nonexistent");
        verify(categoryPersistenceMapper, never()).toDomain(any());
    }

    @Test
    void save_shouldMapToEntityAndSave() {
        // Given
        when(categoryPersistenceMapper.toEntity(domainCategory)).thenReturn(entityCategory);
        when(jpaCategoryRepository.save(entityCategory)).thenReturn(entityCategory);
        when(categoryPersistenceMapper.toDomain(entityCategory)).thenReturn(domainCategory);

        // When
        Category result = categoryRepositoryAdapter.save(domainCategory);

        // Then
        assertThat(result.getId()).isEqualTo("cat-1");
        assertThat(result.getName()).isEqualTo("Action");
        verify(categoryPersistenceMapper).toEntity(domainCategory);
        verify(jpaCategoryRepository).save(entityCategory);
        verify(categoryPersistenceMapper).toDomain(entityCategory);
    }

    @Test
    void delete_shouldCallRepositoryDelete() {
        // Given
        doNothing().when(jpaCategoryRepository).deleteById("cat-1");

        // When
        categoryRepositoryAdapter.delete("cat-1");

        // Then
        verify(jpaCategoryRepository).deleteById("cat-1");
    }

    @Test
    void findByName_shouldReturnDomainModel_whenExists() {
        // Given
        when(jpaCategoryRepository.findByName("Action")).thenReturn(Optional.of(entityCategory));
        when(categoryPersistenceMapper.toDomain(entityCategory)).thenReturn(domainCategory);

        // When
        Optional<Category> result = categoryRepositoryAdapter.findByName("Action");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Action");
        verify(jpaCategoryRepository).findByName("Action");
        verify(categoryPersistenceMapper).toDomain(entityCategory);
    }

    @Test
    void findByName_shouldReturnEmpty_whenNotFound() {
        // Given
        when(jpaCategoryRepository.findByName("NonExistent")).thenReturn(Optional.empty());

        // When
        Optional<Category> result = categoryRepositoryAdapter.findByName("NonExistent");

        // Then
        assertThat(result).isEmpty();
        verify(jpaCategoryRepository).findByName("NonExistent");
        verify(categoryPersistenceMapper, never()).toDomain(any());
    }

    @Test
    void countMoviesByCategoryId_shouldReturnCount() {
        // Given
        when(jpaCategoryRepository.countMoviesByCategoryId("cat-1")).thenReturn(15L);

        // When
        long result = categoryRepositoryAdapter.countMoviesByCategoryId("cat-1");

        // Then
        assertThat(result).isEqualTo(15L);
        verify(jpaCategoryRepository).countMoviesByCategoryId("cat-1");
    }

    @Test
    void findAllOrderBySortOrder_shouldReturnEmptyList_whenNoCategories() {
        // Given
        when(jpaCategoryRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of());
        when(categoryPersistenceMapper.toDomainList(List.of())).thenReturn(List.of());

        // When
        List<Category> result = categoryRepositoryAdapter.findAllOrderBySortOrder();

        // Then
        assertThat(result).isEmpty();
        verify(jpaCategoryRepository).findAllByOrderBySortOrderAsc();
        verify(categoryPersistenceMapper).toDomainList(List.of());
    }

    @Test
    void save_shouldHandleNullDescription() {
        // Given
        Category categoryWithoutDescription = domainCategory.withDescription(null);

        CategoryJpaEntity entityWithoutDescription = CategoryJpaEntity.builder()
                .id("cat-1")
                .name("Action")
                .description(null)
                .megaPath("/Action")
                .sortOrder(1)
                .build();

        when(categoryPersistenceMapper.toEntity(categoryWithoutDescription)).thenReturn(entityWithoutDescription);
        when(jpaCategoryRepository.save(entityWithoutDescription)).thenReturn(entityWithoutDescription);
        when(categoryPersistenceMapper.toDomain(entityWithoutDescription)).thenReturn(categoryWithoutDescription);

        // When
        Category result = categoryRepositoryAdapter.save(categoryWithoutDescription);

        // Then
        assertThat(result.getDescription()).isNull();
        verify(categoryPersistenceMapper).toEntity(categoryWithoutDescription);
        verify(jpaCategoryRepository).save(entityWithoutDescription);
    }

    @Test
    void findAllOrderBySortOrder_shouldPreserveOrderFromRepository() {
        // Given
        Category category2 = Category.builder()
                .id("cat-2")
                .name("Drama")
                .sortOrder(2)
                .build();

        CategoryJpaEntity entity2 = CategoryJpaEntity.builder()
                .id("cat-2")
                .name("Drama")
                .sortOrder(2)
                .build();

        when(jpaCategoryRepository.findAllByOrderBySortOrderAsc())
                .thenReturn(List.of(entityCategory, entity2));
        when(categoryPersistenceMapper.toDomainList(List.of(entityCategory, entity2)))
                .thenReturn(List.of(domainCategory, category2));

        // When
        List<Category> result = categoryRepositoryAdapter.findAllOrderBySortOrder();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSortOrder()).isEqualTo(1);
        assertThat(result.get(1).getSortOrder()).isEqualTo(2);
        verify(jpaCategoryRepository).findAllByOrderBySortOrderAsc();
        verify(categoryPersistenceMapper).toDomainList(List.of(entityCategory, entity2));
    }
}
