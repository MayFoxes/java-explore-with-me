package ru.practicum.ewm.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.users.dto.NewUserRequest;
import ru.practicum.ewm.users.dto.UserDto;
import ru.practicum.ewm.users.dto.UserDtoMapper;
import ru.practicum.ewm.users.model.User;
import ru.practicum.ewm.users.repository.UserRepository;
import ru.practicum.ewm.utility.Pagination;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        Pagination pagination = new Pagination(from, size);
        return ids == null ? UserDtoMapper.toDtos(userRepository.findAll(pagination).stream().collect(Collectors.toList())) :
                UserDtoMapper.toDtos(userRepository.findAllByIdIn(ids, pagination));
    }

    @Override
    public User createUser(NewUserRequest userDto) {
        return userRepository.save(UserDtoMapper.toUser(userDto));

    }

    @Override
    public void deleteUser(Long id) {
        userRepository.delete(checkUserExist(id));
    }

    private User checkUserExist(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(
                        () -> new NotFoundException(String.format("User:%d is not found.", userId)));
    }
}
