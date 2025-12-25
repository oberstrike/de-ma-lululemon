package com.mediaserver.infrastructure.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheStatsDto {
    private long totalSizeBytes;
    private long maxSizeBytes;
    private int usagePercent;
    private long movieCount;
}
