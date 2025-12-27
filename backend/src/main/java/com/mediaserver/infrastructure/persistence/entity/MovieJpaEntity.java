package com.mediaserver.infrastructure.persistence.entity;

import com.mediaserver.domain.model.MovieStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "movies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String title;

    private String description;

    private Integer year;

    private String duration;

    @Column(name = "mega_url")
    private String megaUrl;

    @Column(name = "mega_path")
    private String megaPath;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "local_path")
    private String localPath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "content_type")
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MovieStatus status = MovieStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @ToString.Exclude
    private CategoryJpaEntity category;

    @Column(name = "user_id")
    private String userId;

    @Builder.Default private boolean favorite = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = MovieStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isCached() {
        return localPath != null && status == MovieStatus.READY;
    }
}
