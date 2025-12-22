package com.mediaserver.dto;

import com.mediaserver.entity.MovieStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieDto {
    private String id;
    private String title;
    private String description;
    private Integer year;
    private String duration;
    private String thumbnailUrl;
    private boolean cached;
    private MovieStatus status;
    private String categoryId;
    private String categoryName;
    private Long fileSize;
    private LocalDateTime createdAt;
}
