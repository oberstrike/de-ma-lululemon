package com.mediaserver.application.usecase.download;

import com.mediaserver.domain.model.DownloadTask;
import java.util.List;

public interface GetActiveDownloadsUseCase {
    List<DownloadTask> getActiveDownloads();
}
