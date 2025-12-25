package com.mediaserver.application.usecase.category;

import com.mediaserver.application.command.CreateCategoryCommand;
import com.mediaserver.domain.model.Category;

public interface CreateCategoryUseCase {
    Category createCategory(CreateCategoryCommand command);
}
