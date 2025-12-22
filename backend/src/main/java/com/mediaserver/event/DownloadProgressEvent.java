package com.mediaserver.event;

import com.mediaserver.dto.DownloadProgressDto;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class DownloadProgressEvent extends ApplicationEvent {
    private final DownloadProgressDto progress;

    public DownloadProgressEvent(Object source, DownloadProgressDto progress) {
        super(source);
        this.progress = progress;
    }
}
