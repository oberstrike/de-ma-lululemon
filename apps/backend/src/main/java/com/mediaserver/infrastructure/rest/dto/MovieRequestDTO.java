package com.mediaserver.infrastructure.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class MovieRequestDTO {

    @NotBlank(message = "Title is required") private String title;

    private String description;
    private Integer year;
    private String duration;

    @NotBlank(message = "Mega URL is required") private String megaUrl;

    private String thumbnailUrl;
    private String categoryId;
}
