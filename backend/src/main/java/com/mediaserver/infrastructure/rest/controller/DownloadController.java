package com.mediaserver.infrastructure.rest.controller;

import com.mediaserver.application.usecase.download.GetActiveDownloadsUseCase;
import com.mediaserver.application.usecase.download.GetDownloadProgressUseCase;
import com.mediaserver.infrastructure.rest.dto.DownloadProgressDto;
import com.mediaserver.infrastructure.rest.mapper.DownloadRestMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/downloads")
@RequiredArgsConstructor
public class DownloadController {

    private final GetActiveDownloadsUseCase getActiveDownloadsUseCase;
    private final GetDownloadProgressUseCase getDownloadProgressUseCase;
    private final DownloadRestMapper downloadMapper;

    @GetMapping
    public List<DownloadProgressDto> getActiveDownloads() {
        return getActiveDownloadsUseCase.getActiveDownloads().stream()
                .map(downloadMapper::toResponse)
                .toList();
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<DownloadProgressDto> getDownloadProgress(@PathVariable String movieId) {
        return getDownloadProgressUseCase.getDownloadProgress(movieId)
                .map(downloadMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
