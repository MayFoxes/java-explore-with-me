package ru.practicum.ewm.events.service;

import ru.practicum.ewm.events.dto.AdminEventParams;
import ru.practicum.ewm.events.dto.EventFullDto;
import ru.practicum.ewm.events.dto.EventParams;
import ru.practicum.ewm.events.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.events.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.events.dto.EventShortDto;
import ru.practicum.ewm.events.dto.NewEventDto;
import ru.practicum.ewm.events.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.events.dto.UpdateEventUserRequest;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EventService {

    EventFullDto addNewEvent(Long userId, NewEventDto input);

    List<EventFullDto> getAllEventFromAdmin(AdminEventParams params);

    EventFullDto updateEventFromAdmin(Long eventId, UpdateEventAdminRequest update);

    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size);

    EventFullDto getUserEventById(Long userId, Long eventId);

    EventFullDto updateUserEventById(Long userId, Long eventId, UpdateEventUserRequest update);

    EventRequestStatusUpdateResult updateEventStatusRequest(Long userId, Long eventId, EventRequestStatusUpdateRequest update);

    List<ParticipationRequestDto> getAllRequestsFromEventByOwner(Long userId, Long eventId);

    List<EventShortDto> getAllEvents(EventParams params, HttpServletRequest request);

    EventFullDto getEventById(Long eventId, HttpServletRequest request);

}
