package ru.practicum.ewm.events.controller.pub;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.events.dto.EventFullDto;
import ru.practicum.ewm.events.dto.EventParams;
import ru.practicum.ewm.events.dto.EventShortDto;
import ru.practicum.ewm.events.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/events")
@Slf4j
public class EventPublicController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getAllEvents(@Valid EventParams params,
                                            HttpServletRequest request) {
        log.info("GET request to get all events with params");
        return eventService.getAllEvents(params, request);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventById(@PathVariable Long eventId,
                                     HttpServletRequest request) {
        log.info("GET request to get event:{}", eventId);
        return eventService.getEventById(eventId, request);
    }
}
