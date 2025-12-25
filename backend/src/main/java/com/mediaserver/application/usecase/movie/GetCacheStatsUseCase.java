package com.mediaserver.application.usecase.movie;

import com.mediaserver.infrastructure.rest.dto.CacheStatsDto;

public interface GetCacheStatsUseCase {
    CacheStatsDto getCacheStats();
}
