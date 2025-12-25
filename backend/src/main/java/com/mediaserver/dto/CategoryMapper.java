package com.mediaserver.dto;

import com.mediaserver.entity.Category;
import com.mediaserver.repository.CategoryRepository;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "movieCount", expression = "java((int) categoryRepository.countMoviesByCategoryId(category.getId()))")
    CategoryDto toDto(Category category, @Context CategoryRepository categoryRepository);
}
