package com.mediaserver.application.usecase.download;

import com.mediaserver.entity.DownloadTask;
import java.util.List;

public interface GetActiveDownloadsUseCase {
    List<DownloadTask> getActiveDownloads();
}
