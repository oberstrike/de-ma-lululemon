package com.mediaserver.infrastructure.rest.dto;

import com.mediaserver.domain.model.MovieStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponseDTO {
    private String id;
    private String title;
    private String description;
    private Integer year;
    private String duration;
    private String thumbnailUrl;
    private boolean cached;
    private boolean favorite;
    private MovieStatus status;
    private String categoryId;
    private String categoryName;
    private Long fileSize;
    private LocalDateTime createdAt;
}
