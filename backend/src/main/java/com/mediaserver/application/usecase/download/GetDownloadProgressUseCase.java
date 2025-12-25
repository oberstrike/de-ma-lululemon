package com.mediaserver.application.usecase.download;

import com.mediaserver.domain.model.DownloadTask;
import java.util.Optional;

public interface GetDownloadProgressUseCase {
    Optional<DownloadTask> getDownloadProgress(String movieId);
}
