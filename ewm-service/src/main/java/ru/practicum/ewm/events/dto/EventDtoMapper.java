package ru.practicum.ewm.events.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.category.dto.CategoryDtoMapper;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.users.dto.UserDtoMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class EventDtoMapper {
    public Event toEvent(NewEventDto dto) {
        return Event.builder()
                .annotation(dto.getAnnotation())
                .description(dto.getDescription())
                .eventDate(dto.getEventDate())
                .created(LocalDateTime.now())
                .paid(dto.isPaid())
                .participantLimit(dto.getParticipantLimit())
                .requestModeration(dto.isRequestModeration())
                .title(dto.getTitle())
                .build();
    }

    public EventFullDto toEventFullDto(Event event) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryDtoMapper.toDto(event.getCategory()))
                .createdOn(event.getCreated())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(UserDtoMapper.toShortDto(event.getInitiator()))
                .location(event.getLocation())
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .build();
    }

    public EventShortDto toEventShortDto(Event event) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryDtoMapper.toDto(event.getCategory()))
                .eventDate(event.getEventDate())
                .initiator(UserDtoMapper.toShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .build();
    }

    public Event toUpdate(Event event, UpdateEventRequest request) {
        return Event.builder()
                .id(event.getId())
                .annotation(request.getAnnotation().isBlank() ? event.getAnnotation() : request.getAnnotation())
                .description(request.getDescription().isBlank() ? event.getDescription() : request.getDescription())
                .title(request.getTitle().isBlank() ? event.getTitle() : request.getTitle())
                .location(request.getLocation() == null ? event.getLocation() : request.getLocation())
                .participantLimit(request.getParticipantLimit() == null ? event.getParticipantLimit() : request.getParticipantLimit())
                .paid(request.getPaid().equals(event.getPaid()) ? event.getPaid() : request.getPaid())
                .requestModeration(request.getRequestModeration().equals(event.getRequestModeration()) ? event.getRequestModeration() : request.getRequestModeration())
                .initiator(event.getInitiator())
                .created(event.getCreated())
                .state(event.getState())
                .eventDate(request.getEventDate() == null ? event.getEventDate() : request.getEventDate())
                .build();
    }

    public List<EventShortDto> toEventShortDtos(List<Event> events) {
        return events.stream()
                .map(EventDtoMapper::toEventShortDto)
                .collect(Collectors.toList());
    }
}
