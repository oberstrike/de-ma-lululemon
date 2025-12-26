package com.mediaserver.domain.model;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@Builder
@With
public class Movie {
    String id;
    String title;
    String description;
    Integer year;
    String duration;
    String megaUrl;
    String megaPath;
    String thumbnailUrl;
    String localPath;
    Long fileSize;
    String contentType;
    @Builder.Default MovieStatus status = MovieStatus.PENDING;
    String categoryId;
    String userId;
    @Builder.Default boolean favorite = false;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    /**
     * Checks if the movie is cached locally and ready to stream.
     *
     * @return true if the movie has a local path and is in READY status
     */
    public boolean isCached() {
        return localPath != null && status == MovieStatus.READY;
    }
}
