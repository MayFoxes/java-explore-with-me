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
                .id(null)
                .annotation(dto.getAnnotation())
                .description(dto.getDescription())
                .eventDate(dto.getEventDate())
                .createdDate(LocalDateTime.now())
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
                .createdOn(event.getCreatedDate())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(UserDtoMapper.toShortDto(event.getInitiator()))
                .location(event.getLocation())
                .paid(event.isPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublisherDate())
                .requestModeration(event.isRequestModeration())
                .state(event.getEventStatus())
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
                .paid(event.isPaid())
                .title(event.getTitle())
                .build();
    }

    public List<EventShortDto> toEventShortDtos(List<Event> events) {
        return events.stream()
                .map(EventDtoMapper::toEventShortDto)
                .collect(Collectors.toList());
    }
}
