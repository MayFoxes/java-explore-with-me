package ru.practicum.ewm.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.events.model.enums.EventState;
import ru.practicum.ewm.events.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.dto.RequestDtoMapper;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.enums.RequestStatus;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.users.model.User;
import ru.practicum.ewm.users.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public ParticipationRequestDto addNewRequest(Long userId, Long eventId) {
        User user = checkUser(userId);
        Event event = checkEvent(eventId);
        validateNewRequest(event, userId, eventId);
        Request request = Request.builder()
                .created(LocalDateTime.now())
                .requester(user)
                .event(event)
                .status(event.getRequestModeration() &&
                        event.getParticipantLimit() != 0 ? RequestStatus.PENDING : RequestStatus.CONFIRMED).build();
        requestRepository.save(request);
        return RequestDtoMapper.toParticipationRequestDto(request);
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByUserId(Long userId) {
        checkUser(userId);
        List<Request> result = requestRepository.findAllByRequesterId(userId);
        return result.stream()
                .map(RequestDtoMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        checkUser(userId);
        Request request = checkRequest(requestId, userId);
        if (request.getStatus().equals(RequestStatus.CANCELED) || request.getStatus().equals(RequestStatus.REJECTED)) {
            throw new ValidationException("Request need to be in state of PENDING or CONFIRMED.");
        }
        request.setStatus(RequestStatus.CANCELED);
        Request requestAfterSave = requestRepository.save(request);
        return RequestDtoMapper.toParticipationRequestDto(requestAfterSave);
    }

    private User checkUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("User:%d is not found.", id)));
    }

    private Event checkEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Event:%d is not found.", id)));
    }

    private Request checkRequest(Long requestId, Long userId) {
        return requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException(String.format("Request:%d is not found.", requestId)));
    }

    private void validateNewRequest(Event event, Long userId, Long eventId) {
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException(String.format("User:%d is not a initiator of event:%d.", userId, eventId));
        }
        if (event.getParticipantLimit() > 0 && event.getParticipantLimit() <= requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)) {
            throw new ConflictException("Participant limit is exceeded.");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Event not PUBLISHED.");
        }
        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("This request are already exist.");
        }
    }
}