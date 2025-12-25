package com.mediaserver.application.port.out;

import com.mediaserver.domain.model.DownloadTask;
import java.util.List;
import java.util.Optional;

/**
 * Output port for download task persistence operations. This port will be implemented by the
 * persistence adapter.
 */
public interface DownloadTaskPort {

    /**
     * Finds a download task by its ID.
     *
     * @param id the task ID
     * @return optional containing the task if found
     */
    Optional<DownloadTask> findById(String id);

    /**
     * Finds a download task by movie ID.
     *
     * @param movieId the movie ID
     * @return optional containing the task if found
     */
    Optional<DownloadTask> findByMovieId(String movieId);

    /**
     * Finds all active download tasks.
     *
     * @return list of active tasks
     */
    List<DownloadTask> findActiveTasks();

    /**
     * Saves a download task.
     *
     * @param task the task to save
     * @return the saved task
     */
    DownloadTask save(DownloadTask task);

    /**
     * Deletes a download task.
     *
     * @param task the task to delete
     */
    void delete(DownloadTask task);
}
