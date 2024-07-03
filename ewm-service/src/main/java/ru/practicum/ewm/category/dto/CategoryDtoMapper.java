package ru.practicum.ewm.category.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.category.model.Category;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class CategoryDtoMapper {
    public Category toCategory(NewCategoryDto dto) {
        return Category.builder()
                .name(dto.getName())
                .build();
    }

    public CategoryDto toDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public List<CategoryDto> toDtos(List<Category> categories) {
        return categories.stream()
                .map(CategoryDtoMapper::toDto)
                .collect(Collectors.toList());
    }
}
