package com.mediaserver.application.usecase.category;

import com.mediaserver.domain.model.Category;
import java.util.List;

public interface GetAllCategoriesUseCase {
    List<Category> getAllCategories();
}
