package com.mediaserver.infrastructure.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequestDto {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
    private Integer sortOrder;
}
