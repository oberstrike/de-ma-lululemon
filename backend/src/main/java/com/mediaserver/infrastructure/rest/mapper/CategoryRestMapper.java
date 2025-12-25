package com.mediaserver.infrastructure.rest.mapper;

import com.mediaserver.application.command.CreateCategoryCommand;
import com.mediaserver.application.command.UpdateCategoryCommand;
import com.mediaserver.entity.Category;
import com.mediaserver.infrastructure.rest.dto.CategoryRequestDto;
import com.mediaserver.infrastructure.rest.dto.CategoryResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryRestMapper {

    @Mapping(target = "movieCount", expression = "java(category.getMovies() != null ? category.getMovies().size() : 0)")
    CategoryResponseDto toResponse(Category category);

    CreateCategoryCommand toCreateCommand(CategoryRequestDto dto);

    @Mapping(target = "id", source = "id")
    UpdateCategoryCommand toUpdateCommand(String id, CategoryRequestDto dto);
}
