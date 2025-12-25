package com.mediaserver.infrastructure.persistence.adapter;

import com.mediaserver.domain.model.Movie;
import com.mediaserver.domain.model.MovieStatus;
import com.mediaserver.domain.repository.MovieRepository;
import com.mediaserver.infrastructure.persistence.entity.CategoryJpaEntity;
import com.mediaserver.infrastructure.persistence.entity.MovieJpaEntity;
import com.mediaserver.infrastructure.persistence.mapper.MoviePersistenceMapper;
import com.mediaserver.infrastructure.persistence.repository.JpaCategoryRepository;
import com.mediaserver.infrastructure.persistence.repository.JpaMovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Adapter implementation of MovieRepository port.
 * Bridges the domain layer with the JPA persistence layer.
 */
@Repository
@RequiredArgsConstructor
public class MovieRepositoryAdapter implements MovieRepository {

    private final JpaMovieRepository jpaMovieRepository;
    private final JpaCategoryRepository jpaCategoryRepository;
    private final MoviePersistenceMapper mapper;

    @Override
    public Optional<Movie> findById(String id) {
        return jpaMovieRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Movie> findAll() {
        return mapper.toDomainList(jpaMovieRepository.findAll());
    }

    @Override
    public Movie save(Movie movie) {
        var entity = mapper.toEntity(movie);

        if (movie.getCategoryId() != null) {
            var category = jpaCategoryRepository.findById(movie.getCategoryId())
                    .orElse(null);
            entity.setCategory(category);
        }

        entity.setId(movie.getId());

        var saved = jpaMovieRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void delete(String id) {
        jpaMovieRepository.deleteById(id);
    }

    @Override
    public List<Movie> findByStatus(MovieStatus status) {
        return mapper.toDomainList(jpaMovieRepository.findByStatus(status));
    }

    @Override
    public List<Movie> findByCategoryId(String categoryId) {
        return mapper.toDomainList(jpaMovieRepository.findByCategoryId(categoryId));
    }

    @Override
    public List<Movie> search(String query) {
        return mapper.toDomainList(jpaMovieRepository.search(query));
    }

    @Override
    public List<Movie> findReadyMovies() {
        return mapper.toDomainList(jpaMovieRepository.findReadyMovies());
    }

    @Override
    public List<Movie> findCachedMovies() {
        return mapper.toDomainList(jpaMovieRepository.findCachedMovies());
    }

    @Override
    public Long getTotalCacheSize() {
        return jpaMovieRepository.getTotalCacheSize();
    }

    @Override
    public long countCached() {
        return jpaMovieRepository.countByLocalPathIsNotNull();
    }

    @Override
    public boolean existsByMegaPath(String megaPath) {
        return jpaMovieRepository.existsByMegaPath(megaPath);
    }
}
