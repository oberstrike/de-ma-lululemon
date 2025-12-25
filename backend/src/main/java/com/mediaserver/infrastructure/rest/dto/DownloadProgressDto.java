package com.mediaserver.infrastructure.rest.dto;

import com.mediaserver.domain.model.DownloadStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DownloadProgressDto {
    private String movieId;
    private String movieTitle;
    private DownloadStatus status;
    private long bytesDownloaded;
    private long totalBytes;
    private int progress;
    private String errorMessage;
}
