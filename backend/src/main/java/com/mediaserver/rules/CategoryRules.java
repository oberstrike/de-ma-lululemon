package com.mediaserver.rules;

import com.mediaserver.entity.Category;
import com.mediaserver.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryRules {
    private final CategoryRepository categoryRepository;

    public int movieCount(Category category) {
        return (int) categoryRepository.countMoviesByCategoryId(category.getId());
    }
}
