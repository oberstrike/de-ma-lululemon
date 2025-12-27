package com.mediaserver.infrastructure.persistence.entity;

import com.mediaserver.domain.model.DownloadStatus;
import lombok.*;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "download_tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DownloadTaskJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    @ToString.Exclude
    private MovieJpaEntity movie;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DownloadStatus status = DownloadStatus.QUEUED;

    @Column(name = "bytes_downloaded")
    @Builder.Default
    private Long bytesDownloaded = 0L;

    @Column(name = "total_bytes")
    @Builder.Default
    private Long totalBytes = 0L;

    @Builder.Default private Integer progress = 0;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
