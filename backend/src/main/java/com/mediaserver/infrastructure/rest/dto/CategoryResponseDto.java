package com.mediaserver.infrastructure.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDto {
    private String id;
    private String name;
    private String description;
    private Integer sortOrder;
    private int movieCount;
}
