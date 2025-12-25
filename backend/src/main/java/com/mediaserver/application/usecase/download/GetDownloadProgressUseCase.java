package com.mediaserver.application.usecase.download;

import com.mediaserver.entity.DownloadTask;
import java.util.Optional;

public interface GetDownloadProgressUseCase {
    Optional<DownloadTask> getDownloadProgress(String movieId);
}
