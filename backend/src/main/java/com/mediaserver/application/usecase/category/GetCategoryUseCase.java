package com.mediaserver.application.usecase.category;

import com.mediaserver.entity.Category;

public interface GetCategoryUseCase {
    Category getCategory(String id);
}
