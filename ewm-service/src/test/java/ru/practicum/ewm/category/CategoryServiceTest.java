package ru.practicum.ewm.category;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.EwmServiceMainApp;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.CategoryDtoMapper;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.modul.Category;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.UniqueException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@Transactional
@SpringBootTest(classes = EwmServiceMainApp.class)
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class CategoryServiceTest {
    private final CategoryService categoryService;
    private final NewCategoryDto dto = NewCategoryDto.builder()
            .name("name")
            .build();

    @Test
    void createCategoryTest() {
        Category result = categoryService.createCategory(dto);

        assertEquals(dto.getName(), result.getName());
    }

    @Test
    void getCategoriesTest() {
        Category tempCat = categoryService.createCategory(dto);
        List<CategoryDto> expected = Stream.of(tempCat)
                .map(CategoryDtoMapper::toDto)
                .collect(Collectors.toList());

        List<CategoryDto> actual = categoryService.getAllCategories(0, 10);

        assertIterableEquals(expected, actual);
    }

    @Test
    void getCategoryTest() {
        Category tempCat = categoryService.createCategory(dto);

        CategoryDto dtoCat = categoryService.getCategory(tempCat.getId());

        assertEquals(CategoryDtoMapper.toDto(tempCat), dtoCat);
    }

    @Test
    void getCategoryNotFoundTest() {
        assertThrows(NotFoundException.class, () -> categoryService.getCategory(1L));
    }

    @Test
    void updateCategoryTest() {
        Category cat = categoryService.createCategory(dto);

        NewCategoryDto forUpdate = NewCategoryDto.builder()
                .name("updated")
                .build();
        categoryService.updateCategory(cat.getId(), forUpdate);

        CategoryDto dtoCat = CategoryDto.builder()
                .id(cat.getId())
                .name(forUpdate.getName())
                .build();

        assertEquals(dtoCat, categoryService.getCategory(cat.getId()));
    }

    @Test
    void createCategoryNonUniqueEmailTest() {
        categoryService.createCategory(dto);
        NewCategoryDto sameName = NewCategoryDto.builder()
                .name("name")
                .build();
        UniqueException e = assertThrows(UniqueException.class, () -> categoryService.createCategory(sameName));
        assertEquals("Category name:name is not unique.", e.getMessage());
    }

    @Test
    void deleteCategoryTest() {
        Category result = categoryService.createCategory(dto);
        CategoryDto dtoCat = categoryService.getCategory(result.getId());

        assertEquals(result.getName(), dtoCat.getName());
        assertEquals(dtoCat.getId(), result.getId());

        categoryService.deleteCategory(result.getId());

        NotFoundException e = assertThrows(NotFoundException.class, () -> categoryService.getCategory(result.getId()));
        assertEquals(String.format("Category:%s is not found.", result.getId()), e.getMessage());
    }
}