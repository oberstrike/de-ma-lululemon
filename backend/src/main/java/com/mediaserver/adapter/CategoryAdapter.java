package com.mediaserver.adapter;

import com.mediaserver.dto.CategoryDto;
import com.mediaserver.entity.Category;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

@Component
public class CategoryAdapter {
    private final ConversionService conversionService;

    public CategoryAdapter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public CategoryDto toDto(Category category) {
        return conversionService.convert(category, CategoryDto.class);
    }
}
