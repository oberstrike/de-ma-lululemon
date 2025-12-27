package com.mediaserver.application.usecase.movie;

import com.mediaserver.application.port.in.CacheManagementUseCase;

public interface GetCacheStatsUseCase {
    CacheManagementUseCase.CacheStats getCacheStats();
}
