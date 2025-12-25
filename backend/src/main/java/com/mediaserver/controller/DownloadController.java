package com.mediaserver.controller;

import com.mediaserver.dto.DownloadProgressDto;
import com.mediaserver.adapter.DownloadTaskAdapter;
import com.mediaserver.repository.DownloadTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/downloads")
@RequiredArgsConstructor
public class DownloadController {

    private final DownloadTaskRepository taskRepository;
    private final DownloadTaskAdapter downloadTaskAdapter;

    @GetMapping
    public ResponseEntity<List<DownloadProgressDto>> getActiveDownloads() {
        List<DownloadProgressDto> downloads = taskRepository.findActiveDownloads().stream()
                .map(downloadTaskAdapter::toDto)
                .toList();
        return ResponseEntity.ok(downloads);
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<DownloadProgressDto> getDownloadProgress(@PathVariable String movieId) {
        return taskRepository.findByMovieId(movieId)
                .map(downloadTaskAdapter::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
