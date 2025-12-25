package com.mediaserver.application.usecase.category;

import com.mediaserver.application.command.UpdateCategoryCommand;
import com.mediaserver.entity.Category;

public interface UpdateCategoryUseCase {
    Category updateCategory(UpdateCategoryCommand command);
}
