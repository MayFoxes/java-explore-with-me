package ru.practicum.ewm.events.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.EndpointHit;
import ru.practicum.ewm.ViewStats;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.client.StatsClient;
import ru.practicum.ewm.comment.dto.CommentDtoMapper;
import ru.practicum.ewm.comment.module.Comment;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.events.dto.AdminEventParams;
import ru.practicum.ewm.events.dto.EventDtoMapper;
import ru.practicum.ewm.events.dto.EventFullDto;
import ru.practicum.ewm.events.dto.EventParams;
import ru.practicum.ewm.events.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.events.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.events.dto.EventShortDto;
import ru.practicum.ewm.events.dto.NewEventDto;
import ru.practicum.ewm.events.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.events.dto.UpdateEventRequest;
import ru.practicum.ewm.events.dto.UpdateEventUserRequest;
import ru.practicum.ewm.events.dto.UpdatedStatusDto;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.events.model.Location;
import ru.practicum.ewm.events.model.enums.EventAdminState;
import ru.practicum.ewm.events.model.enums.EventState;
import ru.practicum.ewm.events.model.enums.EventUserState;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private final StatsClient statsClient;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;
    private final ObjectMapper objectMapper;
    private final CommentRepository commentRepository;

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
                    root.get("eventStatus").as(String.class).in(states));
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

    @Transactional
    @Override
    public EventFullDto updateEventFromAdmin(Long eventId, UpdateEventAdminRequest update) {
        Event tempEvent = checkEvent(eventId);
        checkEventState(tempEvent.getEventStatus());
        boolean hasChanges = false;
        Event eventForUpdate = universalUpdate(tempEvent, update);
        if (eventForUpdate == null) {
            eventForUpdate = tempEvent;
        } else {
            hasChanges = true;
        }
        LocalDateTime gotEventDate = update.getEventDate();
        if (gotEventDate != null) {
            if (gotEventDate.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ValidationException("Некорректные параметры даты.Дата начала " +
                        "изменяемого события должна " + "быть не ранее чем за час от даты публикации.");
            }
            eventForUpdate.setEventDate(update.getEventDate());
            hasChanges = true;
        }
        EventAdminState gotAction = update.getStateAction();
        if (gotAction != null) {
            if (EventAdminState.PUBLISH_EVENT.equals(gotAction)) {
                eventForUpdate.setEventStatus(EventState.PUBLISHED);
                hasChanges = true;
            } else if (EventAdminState.REJECT_EVENT.equals(gotAction)) {
                eventForUpdate.setEventStatus(EventState.CANCELED);
                hasChanges = true;
            }
        }
        Event eventAfterUpdate = null;
        if (hasChanges) {
            eventAfterUpdate = eventRepository.save(eventForUpdate);
        }
        return eventAfterUpdate != null ? EventDtoMapper.toEventFullDto(eventAfterUpdate) : null;
    }

    @Transactional
    @Override
    public EventFullDto addNewEvent(Long userId, NewEventDto dto) {
        User user = checkUser(userId);
        checkDateAndTime(dto.getEventDate());
        Event event = EventDtoMapper.toEvent(dto);
        event.setCategory(checkCategory(dto.getCategory()));
        event.setInitiator(user);
        event.setEventStatus(EventState.PENDING);
        event.setLocation(locationRepository.save(dto.getLocation()));
        Event eventSaved = eventRepository.save(event);
        EventFullDto eventFullDto = EventDtoMapper.toEventFullDto(eventSaved);
        eventFullDto.setViews(0L);
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

    @Transactional
    @Override
    public EventFullDto updateUserEventById(Long userId, Long eventId, UpdateEventUserRequest update) {
        checkUser(userId);
        Event oldEvent = checkEventExistForUser(userId, eventId);
        if (oldEvent.getEventStatus().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Статус события не может быть обновлен, так как со статусом PUBLISHED");
        }
        if (!oldEvent.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Пользователь с id= " + userId + " не автор события");
        }
        Event eventForUpdate = universalUpdate(oldEvent, update);
        boolean hasChanges = false;
        if (eventForUpdate == null) {
            eventForUpdate = oldEvent;
        } else {
            hasChanges = true;
        }
        if (update.getEventDate() != null) {
            checkDateAndTime(update.getEventDate());
            eventForUpdate.setEventDate(update.getEventDate());
            hasChanges = true;
        }
        EventUserState stateAction = update.getStateAction();
        if (stateAction != null) {
            switch (stateAction) {
                case SEND_TO_REVIEW:
                    eventForUpdate.setEventStatus(EventState.PENDING);
                    hasChanges = true;
                    break;
                case CANCEL_REVIEW:
                    eventForUpdate.setEventStatus(EventState.CANCELED);
                    hasChanges = true;
                    break;
            }
        }
        Event eventAfterUpdate = null;
        if (hasChanges) {
            eventAfterUpdate = eventRepository.save(eventForUpdate);
        }
        return eventAfterUpdate != null ? EventDtoMapper.toEventFullDto(eventAfterUpdate) : null;
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult updateEventRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest update) {
        checkUser(userId);
        Event tempEvent = checkEventExistForUser(userId, eventId);

        if (!tempEvent.isRequestModeration() || tempEvent.getParticipantLimit() == 0) {
            throw new ConflictException("This event does not require confirmation of requests.");
        }

        RequestStatus status = update.getStatus();

        int confirmedRequestsCount = requestRepository.countByEventIdAndStatus(tempEvent.getId(), RequestStatus.CONFIRMED);

        if (tempEvent.getParticipantLimit() == confirmedRequestsCount) {
            throw new ConflictException("Participants limit has been reached.");
        }

        switch (status) {
            case CONFIRMED:
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
                        .confirmedRequests(confirmedRequests.stream()
                                .map(RequestDtoMapper::toParticipationRequestDto)
                                .collect(Collectors.toList()))
                        .rejectedRequests(rejectedRequests.stream()
                                .map(RequestDtoMapper::toParticipationRequestDto)
                                .collect(Collectors.toList()))
                        .build();
            case REJECTED:
                UpdatedStatusDto updatedStatusReject = updateStatus(tempEvent,
                        UpdatedStatusDto.builder()
                                .updatedIds(update.getRequestIds())
                                .build(),
                        RequestStatus.REJECTED, confirmedRequestsCount);
                List<Request> rejectRequest = requestRepository.findAllById(updatedStatusReject.getProcessedIds());

                return EventRequestStatusUpdateResult.builder()
                        .rejectedRequests(rejectRequest.stream()
                                .map(RequestDtoMapper::toParticipationRequestDto)
                                .collect(Collectors.toList()))
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
    public List<EventShortDto> getAllEvents(EventParams params, HttpServletRequest request, Integer from, Integer size) {
        if (params.getRangeEnd() != null && params.getRangeStart() != null &&
                params.getRangeEnd().isBefore(params.getRangeStart())) {
            throw new ValidationException("End time has to be after Start time.");
        }
        addStatsClient(request);
        Pagination page = new Pagination(from, size);
        Specification<Event> specification = Specification.where(null);

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

        LocalDateTime startDateTime = Objects.requireNonNullElse(params.getRangeStart(), LocalDateTime.now());
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
                criteriaBuilder.equal(root.get("eventStatus"), EventState.PUBLISHED));

        List<Event> resultEvents = eventRepository.findAll(specification, page).getContent();
        List<EventShortDto> result = resultEvents.stream()
                .map(EventDtoMapper::toEventShortDto)
                .collect(Collectors.toList());
        Map<Long, Long> viewStatsMap = getViewsAllEvents(resultEvents);


        for (EventShortDto event : result) {
            event.setViews(viewStatsMap.getOrDefault(event.getId(), 0L));

            List<Comment> commentsFromEvent = commentRepository.findAllByEventId(event.getId(), new Pagination(0, 10));
            event.setComments(CommentDtoMapper.toDtos(commentsFromEvent));
        }
        return result;
    }

    @Override
    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {
        Event event = checkEvent(eventId);
        if (!event.getEventStatus().equals(EventState.PUBLISHED)) {
            throw new NotFoundException(String.format("Event:%d is not PUBLISHED", eventId));
        }
        addStatsClient(request);
        EventFullDto eventFullDto = EventDtoMapper.toEventFullDto(event);
        Map<Long, Long> viewStatsMap = getViewsAllEvents(List.of(event));
        Long views = viewStatsMap.getOrDefault(event.getId(), 0L);
        eventFullDto.setViews(views);
        List<Comment> comments = commentRepository.findAllByEventId(eventId, new Pagination(0, 10));
        eventFullDto.setComments(CommentDtoMapper.toDtos(comments));
        return eventFullDto;
    }

    private Event checkEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event:%d is not found", eventId)));
    }

    private User checkUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User:%d is not found", userId)));
    }

    private Category checkCategory(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException(String.format("Category:%d is not found", catId)));
    }

    private void checkDateAndTime(LocalDateTime dateTime) {
        if (dateTime.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Event date has to be at least 2 hours after current moment");
        }
    }

    private void checkEventState(EventState state) {
        if (state.equals(EventState.PUBLISHED) || state.equals(EventState.CANCELED)) {
            throw new ConflictException("Only unpublished events can be changed");
        }
    }

    private Event checkEventExistForUser(Long userId, Long eventId) {
        return eventRepository.findByInitiatorIdAndId(userId, eventId)
                .orElseThrow(() -> new NotFoundException(String.format("No Event:%d from User:%d was found", eventId, userId)));
    }

    private List<Request> checkRequestOrEventList(Long eventId, List<Long> requestId) {
        return requestRepository.findByEventIdAndIdIn(eventId, requestId).orElseThrow(
                () -> new NotFoundException(String.format("No Request:%s OR Event:%d was found", requestId, eventId)));
    }

    private Map<Long, List<Request>> getConfirmedRequestsCount(List<Event> events) {
        List<Request> requests = requestRepository.findAllByEventIdInAndStatus(events.stream()
                .map(Event::getId)
                .collect(Collectors.toList()), RequestStatus.CONFIRMED);
        return requests.stream()
                .collect(Collectors.groupingBy(r -> r.getEvent().getId()));
    }

    private Map<Long, Long> getViewsAllEvents(List<Event> events) {
        List<String> uris = events.stream()
                .map(event -> String.format("/events/%s", event.getId()))
                .collect(Collectors.toList());

        List<LocalDateTime> startDates = events.stream()
                .map(Event::getCreatedDate)
                .collect(Collectors.toList());
        LocalDateTime earliestDate = startDates.stream()
                .min(LocalDateTime::compareTo)
                .orElse(null);
        Map<Long, Long> viewStatsMap = new HashMap<>();

        if (earliestDate != null) {
            ResponseEntity<Object> response = statsClient.getHit(earliestDate, LocalDateTime.now(),
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
                .timestamp(LocalDateTime.now())
                .build());
    }

    private Event universalUpdate(Event oldEvent, UpdateEventRequest updateEvent) {
        boolean hasChanges = false;
        String gotAnnotation = updateEvent.getAnnotation();
        if (gotAnnotation != null && !gotAnnotation.isBlank()) {
            oldEvent.setAnnotation(gotAnnotation);
            hasChanges = true;
        }
        Long gotCategory = updateEvent.getCategory();
        if (gotCategory != null) {
            Category category = checkCategory(gotCategory);
            oldEvent.setCategory(category);
            hasChanges = true;
        }
        String gotDescription = updateEvent.getDescription();
        if (gotDescription != null && !gotDescription.isBlank()) {
            oldEvent.setDescription(gotDescription);
            hasChanges = true;
        }
        if (updateEvent.getLocation() != null) {
            Location location = updateEvent.getLocation();
            oldEvent.setLocation(location);
            hasChanges = true;
        }
        Integer gotParticipantLimit = updateEvent.getParticipantLimit();
        if (gotParticipantLimit != null) {
            oldEvent.setParticipantLimit(gotParticipantLimit);
            hasChanges = true;
        }
        if (updateEvent.getPaid() != null) {
            oldEvent.setPaid(updateEvent.getPaid());
            hasChanges = true;
        }
        Boolean requestModeration = updateEvent.getRequestModeration();
        if (requestModeration != null) {
            oldEvent.setRequestModeration(requestModeration);
            hasChanges = true;
        }
        String gotTitle = updateEvent.getTitle();
        if (gotTitle != null && !gotTitle.isBlank()) {
            oldEvent.setTitle(gotTitle);
            hasChanges = true;
        }
        if (!hasChanges) {

            oldEvent = null;
        }
        return oldEvent;
    }

}
