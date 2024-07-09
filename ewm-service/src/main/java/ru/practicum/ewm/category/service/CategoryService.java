package ru.practicum.ewm.category.service;

import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.model.Category;

import java.util.List;

public interface CategoryService {
    Category createCategory(NewCategoryDto categoryDto);

    void deleteCategory(Long id);

    Category updateCategory(Long id, NewCategoryDto categoryDto);

    List<CategoryDto> getAllCategories(Integer from, Integer size);

    CategoryDto getCategory(Long id);
}
