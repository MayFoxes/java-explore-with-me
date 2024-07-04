package ru.practicum.ewm.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.service.RequestService;

import javax.validation.constraints.Min;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "users/{userId}/requests")
public class RequestPrivateController {

    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addRequest(@PathVariable @Min(0) Long userId,
                                              @RequestParam @Min(0) Long eventId) {
        log.info("POST request from user:{} to add request for participation in event:{}", eventId, userId);
        return requestService.addNewRequest(userId, eventId);
    }

    @GetMapping
    public List<ParticipationRequestDto> getAllRequests(@PathVariable @Min(0) Long userId) {
        log.info("GET request from user:{} to get all participation request in his events", userId);
        return requestService.getRequestsByUserId(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto canceledRequest(@PathVariable @Min(0) Long userId,
                                                   @PathVariable @Min(0) Long requestId) {
        log.info("PATCH request from user:{} to cancel participation in event", userId);
        return requestService.cancelRequest(userId, requestId);
    }
}
