package com.mediaserver.infrastructure.rest.mapper;

import com.mediaserver.application.command.CreateCategoryCommand;
import com.mediaserver.application.command.UpdateCategoryCommand;
import com.mediaserver.domain.model.Category;
import com.mediaserver.infrastructure.rest.dto.CategoryRequestDTO;
import com.mediaserver.infrastructure.rest.dto.CategoryResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryRestMapper {

    @Mapping(target = "movieCount", ignore = true)
    CategoryResponseDTO toResponse(Category category);

    CreateCategoryCommand toCreateCommand(CategoryRequestDTO dto);

    @Mapping(target = "id", source = "id")
    UpdateCategoryCommand toUpdateCommand(String id, CategoryRequestDTO dto);
}
