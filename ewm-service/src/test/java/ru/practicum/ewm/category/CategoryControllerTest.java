package ru.practicum.ewm.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.category.controller.CategoryController;
import ru.practicum.ewm.category.dto.CategoryDtoMapper;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.modul.Category;
import ru.practicum.ewm.category.service.CategoryService;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CategoryController.class)
public class CategoryControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private CategoryService categoryService;
    private NewCategoryDto dtoCategory;
    private Category category;

    @BeforeEach
    void setUp() {
        dtoCategory = NewCategoryDto.builder()
                .name("Category#1")
                .build();

        category = CategoryDtoMapper.toCategory(dtoCategory);
        category.setId(1L);
    }

    @Test
    @SneakyThrows
    void getCategoriesTest() {
        when(categoryService.getAllCategories(anyInt(), anyInt()))
                .thenReturn(List.of(CategoryDtoMapper.toDto(category)));

        mockMvc.perform(get("/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].id", is(category.getId()), Long.class))
                .andExpect(jsonPath("$.[0].name", is(category.getName())));
    }

    @Test
    @SneakyThrows
    void getCategoryTest() {
        when(categoryService.getCategory(anyLong()))
                .thenReturn(CategoryDtoMapper.toDto(category));

        mockMvc.perform(get("/categories/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(category.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(category.getName())));
    }
}
