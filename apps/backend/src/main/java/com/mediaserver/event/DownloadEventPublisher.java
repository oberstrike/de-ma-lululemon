package com.mediaserver.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DownloadEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleDownloadProgress(DownloadProgressEvent event) {
        messagingTemplate.convertAndSend(
                "/topic/downloads/" + event.getProgress().getMovieId(), event.getProgress());
        messagingTemplate.convertAndSend("/topic/downloads", event.getProgress());
    }
}
