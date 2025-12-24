package com.mediaserver.controller;

import com.mediaserver.dto.DownloadProgressDto;
import com.mediaserver.entity.DownloadTask;
import com.mediaserver.repository.DownloadTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/downloads")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*")
public class DownloadController {

    private final DownloadTaskRepository taskRepository;

    @GetMapping
    public ResponseEntity<List<DownloadProgressDto>> getActiveDownloads() {
        List<DownloadProgressDto> downloads = taskRepository.findActiveDownloads().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(downloads);
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<DownloadProgressDto> getDownloadProgress(@PathVariable String movieId) {
        return taskRepository.findByMovieId(movieId)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private DownloadProgressDto toDto(DownloadTask task) {
        return DownloadProgressDto.builder()
                .movieId(task.getMovie().getId())
                .movieTitle(task.getMovie().getTitle())
                .status(task.getStatus())
                .bytesDownloaded(task.getBytesDownloaded() != null ? task.getBytesDownloaded() : 0)
                .totalBytes(task.getTotalBytes() != null ? task.getTotalBytes() : 0)
                .progress(task.getProgress() != null ? task.getProgress() : 0)
                .errorMessage(task.getErrorMessage())
                .build();
    }
}
