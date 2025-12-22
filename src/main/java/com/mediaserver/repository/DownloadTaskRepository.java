package com.mediaserver.repository;

import com.mediaserver.entity.DownloadStatus;
import com.mediaserver.entity.DownloadTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DownloadTaskRepository extends JpaRepository<DownloadTask, String> {

    Optional<DownloadTask> findByMovieId(String movieId);

    List<DownloadTask> findByStatus(DownloadStatus status);

    @Query("SELECT t FROM DownloadTask t WHERE t.status = 'IN_PROGRESS'")
    List<DownloadTask> findActiveDownloads();
}
