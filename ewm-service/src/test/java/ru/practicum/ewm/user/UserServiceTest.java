package ru.practicum.ewm.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.EwmServiceMainApp;
import ru.practicum.ewm.exception.UniqueException;
import ru.practicum.ewm.users.dto.UserDto;
import ru.practicum.ewm.users.dto.UserDtoMapper;
import ru.practicum.ewm.users.model.User;
import ru.practicum.ewm.users.service.UserService;

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
class UserServiceTest {
    private final UserService userService;
    private final UserDto user = UserDto.builder()
            .id(1L)
            .name("name")
            .email("email@mail.ru")
            .build();

    @Test
    void createUserTest() {
        User result = userService.createUser(user);

        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    void getAllUsersTest() {
        User tempUser = userService.createUser(user);
        List<UserDto> expected = Stream.of(tempUser)
                .map(UserDtoMapper::userToDto)
                .collect(Collectors.toList());

        List<UserDto> actual = userService.getUsers(null, 0, 10);

        assertIterableEquals(expected, actual);
    }

    @Test
    void createUserNonUniqueEmailTest() {
        userService.createUser(user);
        UserDto sameEmailUser = UserDto.builder()
                .name("name5")
                .email(user.getEmail())
                .build();
        assertThrows(UniqueException.class, () -> userService.createUser(sameEmailUser));
    }
}