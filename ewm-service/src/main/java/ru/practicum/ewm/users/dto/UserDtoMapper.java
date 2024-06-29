package ru.practicum.ewm.users.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.users.model.User;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class UserDtoMapper {

    public User dtoToUser(UserDto user) {
        return User.builder()
                .email(user.getEmail())
                .id(user.getId())
                .name(user.getName())
                .build();
    }

    public UserDto userToDto(User user) {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new ValidationException("Email can not be empty!");
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            throw new ValidationException("Name can not be empty!");
        }
        return UserDto.builder()
                .email(user.getEmail())
                .id(user.getId())
                .name(user.getName())
                .build();
    }

    public List<UserDto> toDtos(List<User> users) {
        return users.stream()
                .map(UserDtoMapper::userToDto)
                .collect(Collectors.toList());
    }
}
