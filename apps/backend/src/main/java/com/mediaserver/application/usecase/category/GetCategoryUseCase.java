package com.mediaserver.application.usecase.category;

import com.mediaserver.domain.model.Category;

public interface GetCategoryUseCase {
    Category getCategory(String id);
}
