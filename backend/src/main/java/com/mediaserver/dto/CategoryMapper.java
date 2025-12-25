package com.mediaserver.dto;

import com.mediaserver.entity.Category;
import com.mediaserver.rules.CategoryRules;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "movieCount", expression = "java(rules.movieCount(category))")
    CategoryDto toDto(Category category, @Context CategoryRules rules);
}
