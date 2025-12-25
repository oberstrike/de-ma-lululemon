package com.mediaserver.rules;

import com.mediaserver.entity.DownloadTask;
import com.mediaserver.entity.Movie;
import org.springframework.stereotype.Component;

@Component
public class DownloadTaskRules {
    public long bytesDownloaded(DownloadTask task) {
        return task.getBytesDownloaded() == null ? 0 : task.getBytesDownloaded();
    }

    public long totalBytes(DownloadTask task) {
        return task.getTotalBytes() == null ? 0 : task.getTotalBytes();
    }

    public int progress(DownloadTask task) {
        return task.getProgress() == null ? 0 : task.getProgress();
    }

    public String movieId(DownloadTask task) {
        Movie movie = task.getMovie();
        return movie == null ? null : movie.getId();
    }

    public String movieTitle(DownloadTask task) {
        Movie movie = task.getMovie();
        return movie == null ? null : movie.getTitle();
    }
}
