package ru.practicum.ewm.events.controller.priv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.events.dto.EventFullDto;
import ru.practicum.ewm.events.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.events.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.events.dto.EventShortDto;
import ru.practicum.ewm.events.dto.NewEventDto;
import ru.practicum.ewm.events.dto.UpdateEventUserRequest;
import ru.practicum.ewm.events.service.EventService;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "/users/{userId}/events")
public class EventPrivateController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getAllEventsByUserId(@PathVariable Long userId,
                                                    @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                    @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("GET request from user:{} to get their event", userId);
        return eventService.getUserEvents(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable Long userId,
                                 @RequestBody @Valid NewEventDto dto) {
        log.info("POST request from user:{} to add event:{}", userId, dto.getTitle());
        return eventService.addNewEvent(userId, dto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getFullEventByOwner(@PathVariable Long userId,
                                            @PathVariable Long eventId) {
        log.info("GET request from user:{} to get their event:{}", userId, eventId);
        return eventService.getUserEventById(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByOwner(@PathVariable @Min(0) Long userId,
                                           @PathVariable @Min(0) Long eventId,
                                           @RequestBody @Valid UpdateEventUserRequest update) {
        log.info("PATCH request from user:{} to update their event:{}", userId, eventId);
        return eventService.updateUserEventById(userId, eventId, update);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getAllRequestByOwner(@PathVariable Long userId,
                                                              @PathVariable Long eventId) {
        log.info("GET request from user:{} to get all participation requests from event:{}", userId, eventId);
        return eventService.getAllRequestsFromEventByOwner(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateStatusRequest(@PathVariable Long userId,
                                                              @PathVariable Long eventId,
                                                              @RequestBody EventRequestStatusUpdateRequest update) {
        log.info("PATCH request from user:{} to update event status for event:{}", userId, eventId);
        return eventService.updateEventStatusRequest(userId, eventId, update);
    }
}
