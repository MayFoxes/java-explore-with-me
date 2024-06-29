package ru.practicum.ewm.users.service;

import ru.practicum.ewm.users.dto.UserDto;
import ru.practicum.ewm.users.model.User;

import java.util.List;

public interface UserService {
    List<UserDto> getUsers(List<Integer> ids, Integer from, Integer size);

    User createUser(UserDto userDto);

    void deleteUser(Long id);
}
