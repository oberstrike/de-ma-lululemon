package com.mediaserver.dto;

import com.mediaserver.entity.Category;
import com.mediaserver.repository.CategoryRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CategoryToDtoConverter implements Converter<Category, CategoryDto> {
    private final CategoryMapper categoryMapper;
    private final CategoryRepository categoryRepository;

    public CategoryToDtoConverter(CategoryMapper categoryMapper, CategoryRepository categoryRepository) {
        this.categoryMapper = categoryMapper;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CategoryDto convert(Category source) {
        return categoryMapper.toDto(source, categoryRepository);
    }
}
