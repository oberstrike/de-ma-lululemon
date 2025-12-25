package com.mediaserver.application.usecase.category;

import com.mediaserver.application.command.CreateCategoryCommand;
import com.mediaserver.entity.Category;

public interface CreateCategoryUseCase {
    Category createCategory(CreateCategoryCommand command);
}
