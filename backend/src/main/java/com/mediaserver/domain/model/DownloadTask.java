package com.mediaserver.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.LocalDateTime;

@Value
@Builder
@With
public class DownloadTask {
    String id;
    String movieId;
    @Builder.Default
    DownloadStatus status = DownloadStatus.QUEUED;
    @Builder.Default
    Long bytesDownloaded = 0L;
    @Builder.Default
    Long totalBytes = 0L;
    @Builder.Default
    Integer progress = 0;
    String errorMessage;
    LocalDateTime startedAt;
    LocalDateTime completedAt;
}
