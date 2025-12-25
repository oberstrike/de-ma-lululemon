package com.mediaserver.dto;

import com.mediaserver.entity.DownloadTask;
import com.mediaserver.rules.DownloadTaskRules;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DownloadTaskMapperDelegate {
    private final DownloadTaskMapper delegate;
    private final DownloadTaskRules rules;

    public DownloadProgressDto toDto(DownloadTask task) {
        return delegate.toDto(task, rules);
    }
}
