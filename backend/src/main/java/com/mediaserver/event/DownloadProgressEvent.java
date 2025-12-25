package com.mediaserver.event;

import com.mediaserver.infrastructure.rest.dto.DownloadProgressDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class DownloadProgressEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;
    private final transient DownloadProgressDTO progress;

    public DownloadProgressEvent(Object source, DownloadProgressDTO progress) {
        super(source);
        this.progress = progress;
    }
}
