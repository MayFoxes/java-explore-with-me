package ru.practicum.ewm.events.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.EndpointHit;
import ru.practicum.ewm.ViewStats;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.client.StatsClient;
import ru.practicum.ewm.events.dto.AdminEventParams;
import ru.practicum.ewm.events.dto.EventDtoMapper;
import ru.practicum.ewm.events.dto.EventFullDto;
import ru.practicum.ewm.events.dto.EventParams;
import ru.practicum.ewm.events.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.events.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.events.dto.EventShortDto;
import ru.practicum.ewm.events.dto.NewEventDto;
import ru.practicum.ewm.events.dto.UpdateEventRequest;
import ru.practicum.ewm.events.dto.UpdatedStatusDto;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.events.model.enums.EventState;
import ru.practicum.ewm.events.model.enums.UpdateEventState;
import ru.practicum.ewm.events.repository.EventRepository;
import ru.practicum.ewm.events.repository.LocationRepository;
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
import ru.practicum.ewm.utility.Pagination;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final StatsClient statsClient;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<EventFullDto> getAllEventFromAdmin(AdminEventParams params) {
        Pagination pagination = new Pagination(params.getFrom(), params.getSize());

        Specification<Event> spec = Specification.where(null);

        List<Long> users = params.getUsers();
        List<String> states = params.getStates();
        List<Long> categories = params.getCategories();
        LocalDateTime rangeEnd = params.getRangeEnd();
        LocalDateTime rangeStart = params.getRangeStart();

        if (users != null && !users.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    root.get("initiator").get("id").in(users));
        }
        if (states != null && !states.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    root.get("state").as(String.class).in(states));
        }
        if (categories != null && !categories.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    root.get("category").get("id").in(categories));
        }
        if (rangeEnd != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
        }
        if (rangeStart != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
        }

        Page<Event> events = eventRepository.findAll(spec, pagination);

        List<EventFullDto> result = events.getContent().stream()
                .map(EventDtoMapper::toEventFullDto)
                .collect(Collectors.toList());

        Map<Long, List<Request>> confirmedRequestsCount = getConfirmedRequestsCount(events.toList());
        for (EventFullDto event : result) {
            List<Request> requests = confirmedRequestsCount.getOrDefault(event.getId(), List.of());
            event.setConfirmedRequests(requests.size());
        }
        return result;
    }

    @Override
    public EventFullDto updateEventFromAdmin(Long eventId, UpdateEventRequest update) {
        Event tempEvent = checkEvent(eventId);
        checkEventState(tempEvent.getState());
        Category cat = checkCategory(update.getCategory());
        checkDateAndTime(update.getEventDate());
        Event updatedEvent = EventDtoMapper.toUpdate(tempEvent, update);
        updatedEvent.setCategory(cat);
        UpdateEventState state = update.getState();

        if (state != null) {
            if (state.equals(UpdateEventState.PUBLISH_EVENT)) {
                updatedEvent.setState(EventState.PUBLISHED);
                updatedEvent.setPublishedOn(LocalDateTime.now());
            } else if (state.equals(UpdateEventState.REJECT_EVENT)) {
                updatedEvent.setState(EventState.CANCELED);
            }
        }

        return EventDtoMapper.toEventFullDto(eventRepository.save(updatedEvent));
    }

    @Override
    public EventFullDto addNewEvent(Long userId, NewEventDto dto) {
        User user = checkUser(userId);
        checkDateAndTime(dto.getEventDate());
        Event event = EventDtoMapper.toEvent(dto);
        event.setCategory(checkCategory(dto.getCategory()));
        event.setInitiator(user);
        event.setState(EventState.PENDING);
        event.setLocation(locationRepository.save(dto.getLocation()));
        Event eventSaved = eventRepository.save(event);
        EventFullDto eventFullDto = EventDtoMapper.toEventFullDto(eventSaved);
        eventFullDto.setViews(0);
        eventFullDto.setConfirmedRequests(0);
        return eventFullDto;
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
        checkUser(userId);
        Pagination pagination = new Pagination(from, size);
        return EventDtoMapper.toEventShortDtos(eventRepository.findAllByInitiatorId(userId, pagination));
    }

    @Override
    public EventFullDto getUserEventById(Long userId, Long eventId) {
        checkUser(userId);
        return EventDtoMapper.toEventFullDto(checkEventExistForUser(userId, eventId));
    }

    @Override
    public EventFullDto updateUserEventById(Long userId, Long eventId, UpdateEventRequest update) {
        checkUser(userId);
        Event tempEvent = checkEventExistForUser(userId, eventId);
        if (tempEvent.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Статус события не может быть обновлен, так как со статусом PUBLISHED");
        }
        if (!tempEvent.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Пользователь с id= " + userId + " не автор события");
        }
        Event updatedEv = EventDtoMapper.toUpdate(tempEvent, update);
        checkDateAndTime(updatedEv.getEventDate());
        UpdateEventState stateAction = update.getState();
        if (stateAction != null) {
            switch (stateAction) {
                case SEND_TO_REVIEW:
                    updatedEv.setState(EventState.PENDING);
                    break;
                case CANCEL_REVIEW:
                    updatedEv.setState(EventState.CANCELED);
                    break;
            }
        }

        return EventDtoMapper.toEventFullDto(eventRepository.save(updatedEv));
    }

    @Override
    public EventRequestStatusUpdateResult updateEventStatusRequest(Long userId, Long eventId, EventRequestStatusUpdateRequest update) {
        checkUser(userId);
        Event tempEvent = checkEventExistForUser(userId, eventId);

        if (!tempEvent.getRequestModeration() || tempEvent.getParticipantLimit() == 0) {
            throw new ConflictException("This event does not require confirmation of requests.");
        }

        RequestStatus status = update.getStatus();

        int confirmedRequestsCount = requestRepository.countByEventIdAndStatus(tempEvent.getId(), RequestStatus.CONFIRMED);
        switch (status) {
            case CONFIRMED:
                if (tempEvent.getParticipantLimit() == confirmedRequestsCount) {
                    throw new ConflictException("Participants limit has been reached.");
                }
                UpdatedStatusDto updatedStatusConfirm = updateStatus(tempEvent,
                        UpdatedStatusDto.builder()
                                .updatedIds(update.getRequestIds())
                                .build(),
                        RequestStatus.CONFIRMED, confirmedRequestsCount);

                List<Request> confirmedRequests = requestRepository.findAllById(updatedStatusConfirm.getProcessedIds());
                List<Request> rejectedRequests = new ArrayList<>();
                if (!updatedStatusConfirm.getUpdatedIds().isEmpty()) {
                    List<Long> ids = updatedStatusConfirm.getUpdatedIds();
                    rejectedRequests = rejectRequest(ids, eventId);
                }

                return EventRequestStatusUpdateResult.builder()
                        .confirmedRequests(confirmedRequests
                                .stream()
                                .map(RequestDtoMapper::toParticipationRequestDto).collect(Collectors.toList()))
                        .rejectedRequests(rejectedRequests
                                .stream()
                                .map(RequestDtoMapper::toParticipationRequestDto).collect(Collectors.toList()))
                        .build();
            case REJECTED:
                if (tempEvent.getParticipantLimit() == confirmedRequestsCount) {
                    throw new ConflictException("Participants limit has been reached.");
                }
                UpdatedStatusDto updatedStatusReject = updateStatus(tempEvent,
                        UpdatedStatusDto.builder()
                                .updatedIds(update.getRequestIds())
                                .build(),
                        RequestStatus.REJECTED, confirmedRequestsCount);
                List<Request> rejectRequest = requestRepository.findAllById(updatedStatusReject.getProcessedIds());

                return EventRequestStatusUpdateResult.builder()
                        .rejectedRequests(rejectRequest
                                .stream()
                                .map(RequestDtoMapper::toParticipationRequestDto).collect(Collectors.toList()))
                        .build();
            default:
                throw new ValidationException("Incorrect status:" + status);
        }
    }

    @Override
    public List<ParticipationRequestDto> getAllRequestsFromEventByOwner(Long userId, Long eventId) {
        checkUser(userId);
        checkEventExistForUser(userId, eventId);
        List<Request> requests = requestRepository.findAllByEventId(eventId);
        return requests.stream()
                .map(RequestDtoMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventShortDto> getAllEvents(EventParams params, HttpServletRequest request) {
        if (params.getRangeEnd() != null && params.getRangeStart() != null &&
                params.getRangeEnd().isBefore(params.getRangeStart())) {
            throw new ValidationException("End time has to be after Start time.");
        }
        addStatsClient(request);
        Pageable pageable = PageRequest.of(params.getFrom(), params.getSize());
        Specification<Event> specification = Specification.where(null);
        LocalDateTime now = LocalDateTime.now();

        if (params.getText() != null) {
            String searchText = params.getText().toLowerCase();
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), "%" + searchText + "%"),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + searchText + "%")
                    ));
        }

        if (params.getCategories() != null && !params.getCategories().isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("category").get("id").in(params.getCategories()));
        }

        LocalDateTime startDateTime = Objects.requireNonNullElse(params.getRangeStart(), now);
        specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThan(root.get("eventDate"), startDateTime));

        if (params.getRangeEnd() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThan(root.get("eventDate"), params.getRangeEnd()));
        }

        if (params.getOnlyAvailable() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("participantLimit"), 0));
        }

        specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("state"), EventState.PUBLISHED));

        List<Event> resultEvents = eventRepository.findAll(specification, pageable).getContent();
        List<EventShortDto> result = resultEvents.stream()
                .map(EventDtoMapper::toEventShortDto)
                .collect(Collectors.toList());
        Map<Long, Integer> viewStatsMap = getViewsAllEvents(resultEvents);

        for (EventShortDto event : result) {
            Integer viewsFromMap = viewStatsMap.getOrDefault(event.getId(), 0);
            event.setViews(viewsFromMap);
        }
        return result;
    }

    @Override
    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {
        Event event = checkEvent(eventId);
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException(String.format("Event:%d is not PUBLISHED.", eventId));
        }

        EventFullDto eventFullDto = EventDtoMapper.toEventFullDto(event);
        Map<Long, Integer> viewStatsMap = getViewsAllEvents(List.of(event));
        Integer views = viewStatsMap.getOrDefault(event.getId(), 0);
        eventFullDto.setViews(views);
        return eventFullDto;
    }

    private Event checkEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Event:%d is not found.", id)));
    }

    private User checkUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("User:%d is not found.", id)));
    }

    private Category checkCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Category:%d is not found.", id)));
    }

    private void checkDateAndTime(LocalDateTime dateTime) {
        if (dateTime.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Event date has to be at least 2 hours after current moment");
        }
    }

    private void checkEventState(EventState state) {
        if (state.equals(EventState.PUBLISHED) || state.equals(EventState.CANCELED)) {
            throw new ConflictException("Only unpublished events can be changed.");
        }
    }

    private Event checkEventExistForUser(Long userId, Long eventId) {
        return eventRepository.findByInitiatorIdAndId(userId, eventId)
                .orElseThrow(() -> new NotFoundException(String.format("No Event:%d from User:%d was found.", eventId, userId)));
    }

    private List<Request> checkRequestOrEventList(Long eventId, List<Long> requestId) {
        return requestRepository.findByEventIdAndIdIn(eventId, requestId).orElseThrow(
                () -> new NotFoundException(String.format("No Request:%s OR Event:%d was found.", requestId, eventId)));
    }

    private Map<Long, List<Request>> getConfirmedRequestsCount(List<Event> events) {
        List<Request> requests = requestRepository.findAllByEventIdInAndStatus(events.stream()
                .map(Event::getId)
                .collect(Collectors.toList()), RequestStatus.CONFIRMED);
        return requests.stream()
                .collect(Collectors.groupingBy(r -> r.getEvent().getId()));
    }

    private Map<Long, Integer> getViewsAllEvents(List<Event> events) {
        List<String> uris = events.stream()
                .map(event -> String.format("/events/%s", event.getId()))
                .collect(Collectors.toList());

        List<LocalDateTime> startDates = events.stream()
                .map(Event::getCreated)
                .collect(Collectors.toList());
        LocalDateTime earliestDate = startDates.stream()
                .min(LocalDateTime::compareTo)
                .orElse(null);
        Map<Long, Integer> viewStatsMap = new HashMap<>();

        if (earliestDate != null) {
            ResponseEntity<Object> response = statsClient.getHit(earliestDate.toString(), LocalDateTime.now().toString(),
                    uris, true);

            List<ViewStats> viewStatsList = objectMapper.convertValue(response.getBody(), new TypeReference<>() {
            });

            viewStatsMap = viewStatsList.stream()
                    .filter(statsDto -> statsDto.getUri().startsWith("/events/"))
                    .collect(Collectors.toMap(
                            statsDto -> Long.parseLong(statsDto.getUri().substring("/events/".length())),
                            ViewStats::getHits
                    ));
        }
        return viewStatsMap;
    }

    private UpdatedStatusDto updateStatus(Event event,
                                          UpdatedStatusDto updatedIds,
                                          RequestStatus status,
                                          Integer confirmedRequestsCount) {
        int freeRequest = event.getParticipantLimit() - confirmedRequestsCount;
        List<Request> requestListLoaded = checkRequestOrEventList(event.getId(), updatedIds.getUpdatedIds());
        List<Long> processedIds = new ArrayList<>();
        List<Request> requests = new ArrayList<>();

        for (Request request : requestListLoaded) {
            if (freeRequest == 0) {
                break;
            }
            request.setStatus(status);
            requests.add(request);
            processedIds.add(request.getId());
            freeRequest--;
        }
        requestRepository.saveAll(requests);
        updatedIds.setProcessedIds(processedIds);
        return updatedIds;
    }

    private List<Request> rejectRequest(List<Long> ids, Long eventId) {
        List<Request> rejectedRequests = new ArrayList<>();
        List<Request> requestList = new ArrayList<>();
        List<Request> requestListLoaded = checkRequestOrEventList(eventId, ids);

        for (Request request : requestListLoaded) {
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                break;
            }
            request.setStatus(RequestStatus.REJECTED);
            requestList.add(request);
            rejectedRequests.add(request);
        }
        requestRepository.saveAll(requestList);
        return rejectedRequests;
    }

    private void addStatsClient(HttpServletRequest request) {
        statsClient.saveHit(EndpointHit.builder()
                .app("ewm-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build());
    }

}
