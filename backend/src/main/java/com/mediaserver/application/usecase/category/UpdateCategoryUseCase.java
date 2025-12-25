package com.mediaserver.application.usecase.category;

import com.mediaserver.application.command.UpdateCategoryCommand;
import com.mediaserver.domain.model.Category;

public interface UpdateCategoryUseCase {
    Category updateCategory(UpdateCategoryCommand command);
}
