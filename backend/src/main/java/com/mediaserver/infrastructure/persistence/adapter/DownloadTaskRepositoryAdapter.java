package com.mediaserver.infrastructure.persistence.adapter;

import com.mediaserver.domain.model.DownloadTask;
import com.mediaserver.domain.repository.DownloadTaskRepository;
import com.mediaserver.infrastructure.persistence.entity.DownloadTaskJpaEntity;
import com.mediaserver.infrastructure.persistence.entity.MovieJpaEntity;
import com.mediaserver.infrastructure.persistence.mapper.DownloadTaskPersistenceMapper;
import com.mediaserver.infrastructure.persistence.repository.JpaDownloadTaskRepository;
import com.mediaserver.infrastructure.persistence.repository.JpaMovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Adapter implementation of DownloadTaskRepository port.
 * Bridges the domain layer with the JPA persistence layer.
 */
@Repository
@RequiredArgsConstructor
public class DownloadTaskRepositoryAdapter implements DownloadTaskRepository {

    private final JpaDownloadTaskRepository jpaDownloadTaskRepository;
    private final JpaMovieRepository jpaMovieRepository;
    private final DownloadTaskPersistenceMapper mapper;

    @Override
    public Optional<DownloadTask> findById(String id) {
        return jpaDownloadTaskRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<DownloadTask> findByMovieId(String movieId) {
        return jpaDownloadTaskRepository.findByMovieId(movieId)
                .map(mapper::toDomain);
    }

    @Override
    public DownloadTask save(DownloadTask task) {
        DownloadTaskJpaEntity entity = mapper.toEntity(task);

        // Set the movie relationship if movieId is present
        if (task.getMovieId() != null) {
            MovieJpaEntity movie = jpaMovieRepository.findById(task.getMovieId())
                    .orElseThrow(() -> new IllegalArgumentException("Movie not found with id: " + task.getMovieId()));
            entity.setMovie(movie);
        }

        // If updating an existing task, preserve the ID
        if (task.getId() != null) {
            entity.setId(task.getId());
        }

        DownloadTaskJpaEntity saved = jpaDownloadTaskRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void delete(String id) {
        jpaDownloadTaskRepository.deleteById(id);
    }

    @Override
    public List<DownloadTask> findActiveDownloads() {
        return mapper.toDomainList(jpaDownloadTaskRepository.findActiveDownloads());
    }
}
