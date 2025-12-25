package com.mediaserver.domain.repository;

import com.mediaserver.domain.model.DownloadTask;
import java.util.List;
import java.util.Optional;

/**
 * Repository port interface for DownloadTask domain entity.
 * This is a port in hexagonal architecture - implementations are adapters.
 */
public interface DownloadTaskRepository {

    /**
     * Find a download task by its ID.
     * @param id the task ID
     * @return Optional containing the task if found
     */
    Optional<DownloadTask> findById(String id);

    /**
     * Find a download task by movie ID.
     * @param movieId the movie ID
     * @return Optional containing the task if found
     */
    Optional<DownloadTask> findByMovieId(String movieId);

    /**
     * Save a download task (create or update).
     * @param task the task to save
     * @return the saved task
     */
    DownloadTask save(DownloadTask task);

    /**
     * Delete a download task by its ID.
     * @param id the task ID
     */
    void delete(String id);

    /**
     * Find all active (in-progress) downloads.
     * @return list of active download tasks
     */
    List<DownloadTask> findActiveDownloads();
}
