package com.mediaserver.infrastructure.persistence.adapter;

import com.mediaserver.application.port.out.MoviePort;
import com.mediaserver.domain.model.Movie;
import com.mediaserver.domain.model.MovieStatus;
import com.mediaserver.domain.repository.MovieRepository;
import com.mediaserver.infrastructure.persistence.entity.MovieFavoriteJpaEntity;
import com.mediaserver.infrastructure.persistence.mapper.MoviePersistenceMapper;
import com.mediaserver.infrastructure.persistence.repository.JpaCategoryRepository;
import com.mediaserver.infrastructure.persistence.repository.JpaMovieFavoriteRepository;
import com.mediaserver.infrastructure.persistence.repository.JpaMovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Adapter implementation of MovieRepository port. Bridges the domain layer with the JPA persistence
 * layer.
 */
@Repository
@RequiredArgsConstructor
public class MovieRepositoryAdapter implements MovieRepository, MoviePort {

    private final JpaMovieRepository jpaMovieRepository;
    private final JpaCategoryRepository jpaCategoryRepository;
    private final JpaMovieFavoriteRepository jpaMovieFavoriteRepository;
    private final MoviePersistenceMapper mapper;

    @Override
    public Optional<Movie> findById(String id) {
        return jpaMovieRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Movie> findAll() {
        return mapper.toDomainList(jpaMovieRepository.findAll());
    }

    @Override
    public Movie save(Movie movie) {
        var entity = mapper.toEntity(movie);

        if (movie.getCategoryId() != null) {
            var category = jpaCategoryRepository.findById(movie.getCategoryId()).orElse(null);
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
    public void delete(Movie movie) {
        if (movie.getId() != null) {
            jpaMovieRepository.deleteById(movie.getId());
        }
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
    public long getTotalCacheSize() {
        Long size = jpaMovieRepository.getTotalCacheSize();
        return size != null ? size : 0L;
    }

    @Override
    public long countCached() {
        return jpaMovieRepository.countByLocalPathIsNotNull();
    }

    @Override
    public long countCachedMovies() {
        return jpaMovieRepository.countByLocalPathIsNotNull();
    }

    @Override
    public boolean existsByMegaPath(String megaPath) {
        return jpaMovieRepository.existsByMegaPath(megaPath);
    }

    @Override
    public List<Movie> findFavorites(String userId) {
        return mapper.toDomainList(jpaMovieRepository.findFavoritesByUserId(userId)).stream()
                .map(movie -> movie.withFavorite(true))
                .collect(Collectors.toList());
    }

    @Override
    public void addFavorite(String movieId, String userId) {
        if (jpaMovieFavoriteRepository.existsByMovie_IdAndUserId(movieId, userId)) {
            return;
        }
        var movieReference = jpaMovieRepository.getReferenceById(movieId);
        var favorite =
                MovieFavoriteJpaEntity.builder().movie(movieReference).userId(userId).build();
        jpaMovieFavoriteRepository.save(favorite);
    }

    @Override
    public void removeFavorite(String movieId, String userId) {
        jpaMovieFavoriteRepository.deleteByMovie_IdAndUserId(movieId, userId);
    }

    @Override
    public boolean isFavorite(String movieId, String userId) {
        return jpaMovieFavoriteRepository.existsByMovie_IdAndUserId(movieId, userId);
    }

    @Override
    public List<Movie> applyFavoriteStatus(List<Movie> movies, String userId) {
        if (movies.isEmpty()) {
            return movies;
        }
        Set<String> favoriteIds =
                jpaMovieRepository.findFavoritesByUserId(userId).stream()
                        .map(entity -> entity.getId())
                        .collect(Collectors.toSet());
        return movies.stream()
                .map(movie -> movie.withFavorite(favoriteIds.contains(movie.getId())))
                .toList();
    }

    @Override
    public List<Movie> findCachedNonFavorites() {
        return mapper.toDomainList(jpaMovieRepository.findCachedNonFavoriteMovies());
    }
}
