package com.mediaserver.infrastructure.persistence.repository;

import com.mediaserver.domain.model.DownloadStatus;
import com.mediaserver.infrastructure.persistence.entity.DownloadTaskJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for DownloadTaskJpaEntity.
 * This is an infrastructure component that provides persistence operations.
 */
public interface JpaDownloadTaskRepository extends JpaRepository<DownloadTaskJpaEntity, String> {

    Optional<DownloadTaskJpaEntity> findByMovieId(String movieId);

    @Query("SELECT t FROM DownloadTaskJpaEntity t WHERE t.status = :status")
    List<DownloadTaskJpaEntity> findByStatus(@Param("status") DownloadStatus status);

    default List<DownloadTaskJpaEntity> findActiveDownloads() {
        return findByStatus(DownloadStatus.IN_PROGRESS);
    }
}
