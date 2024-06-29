package ru.practicum.ewm.admin;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.modul.Category;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.users.dto.UserDto;
import ru.practicum.ewm.users.model.User;
import ru.practicum.ewm.users.service.UserService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/admin")
@AllArgsConstructor
@Slf4j
public class AdminController {
    private final UserService userService;
    private final CategoryService categoryService;

    @PostMapping("/categories")
    public Category createCategoryByAdmin(@Valid @RequestBody NewCategoryDto dto) {
        log.info("POST request from Admin to add category with name:{}.", dto.getName());
        return categoryService.createCategory(dto);
    }

    @DeleteMapping("/categories/{catId}")
    public void deleteCategoryByAdmin(@PathVariable Long catId) {
        log.info("DELETE request from Admin to delete category:{}.", catId);
        categoryService.deleteCategory(catId);
    }

    @PutMapping("/categories/{catId}")
    public Category updateCategoryByAdmin(@PathVariable Long catId,
                                          @Valid @RequestBody NewCategoryDto dto
    ) {
        log.info("PUT request from Admin to update category:{}.", catId);
        return categoryService.updateCategory(catId, dto);
    }

    @GetMapping("/users")
    public List<UserDto> getUsers(@RequestParam(defaultValue = "0") Integer from,
                                  @RequestParam(defaultValue = "10") Integer size,
                                  @RequestParam(required = false) List<Integer> ids
    ) {
        log.info("GET request from Admin to get users IN:{} from:{} with size:{}", ids, from, size);
        return userService.getUsers(ids, from, size);
    }

    @PostMapping("/users")
    public User addUser(@Valid @RequestBody UserDto user) {
        log.info("POST request from Admin to add user");
        return userService.createUser(user);
    }

    @DeleteMapping("/users/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        log.info("DELETE request from Admin to delete user:{}", userId);
        userService.deleteUser(userId);
    }

}
