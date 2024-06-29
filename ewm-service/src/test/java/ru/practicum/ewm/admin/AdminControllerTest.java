package ru.practicum.ewm.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.category.dto.CategoryDtoMapper;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.modul.Category;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.users.dto.UserDto;
import ru.practicum.ewm.users.dto.UserDtoMapper;
import ru.practicum.ewm.users.model.User;
import ru.practicum.ewm.users.service.UserService;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminController.class)
class AdminControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserService userService;
    @MockBean
    private CategoryService categoryService;
    private UserDto dtoUser;
    private User user;
    private NewCategoryDto dtoCategory;
    private Category category;

    @BeforeEach
    void setUp() {
        dtoUser = UserDto.builder()
                .id(1L)
                .name("name")
                .email("email@mail.ru")
                .build();
        user = UserDtoMapper.dtoToUser(dtoUser);

        dtoCategory = NewCategoryDto.builder()
                .name("Category#1")
                .build();

        category = CategoryDtoMapper.toCategory(dtoCategory);
        category.setId(1L);
    }

    @Test
    @SneakyThrows
    void createUserByAdminTest() {
        when(userService.createUser(dtoUser))
                .thenReturn(user);

        mockMvc.perform(post("/admin/users")
                        .content(objectMapper.writeValueAsString(dtoUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(user.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(user.getName())))
                .andExpect(jsonPath("$.email", is(user.getEmail())));
    }


    @Test
    @SneakyThrows
    void getAllUsersByAdminTest() {
        when(userService.getUsers(any(), anyInt(), anyInt()))
                .thenReturn(List.of(UserDtoMapper.userToDto(user)));

        mockMvc.perform(get("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].id", is(user.getId()), Long.class))
                .andExpect(jsonPath("$.[0].name", is(user.getName())))
                .andExpect(jsonPath("$.[0].email", is(user.getEmail())));
    }

    @Test
    @SneakyThrows
    void deleteUserByAdminTest() {

        mockMvc.perform(delete("/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    @Test
    @SneakyThrows
    void createCategoryByAdminTest() {
        when(categoryService.createCategory(dtoCategory))
                .thenReturn(category);

        mockMvc.perform(post("/admin/categories")
                        .content(objectMapper.writeValueAsString(dtoCategory))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(category.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(category.getName())));
    }

    @Test
    @SneakyThrows
    void deleteCategoryByAdminTest() {

        mockMvc.perform(delete("/admin/categories/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void updateCategoryByAdminTest() {
        when(categoryService.updateCategory(anyLong(), any(NewCategoryDto.class)))
                .thenReturn(category);

        dtoCategory.setName("NewCategoryFor#1");

        mockMvc.perform(put("/admin/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoCategory)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(category.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(category.getName())));
    }

}