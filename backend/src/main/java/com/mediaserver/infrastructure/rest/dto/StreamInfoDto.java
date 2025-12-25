package com.mediaserver.infrastructure.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamInfoDto {
    private String movieId;
    private String title;
    private long fileSize;
    private String contentType;
    private String streamUrl;
    private boolean supportsRangeRequests;
}
