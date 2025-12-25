package com.mediaserver.dto;

import lombok.Delegate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DownloadTaskMapperDelegate {
    @Delegate
    private final DownloadTaskMapper delegate;
}
