package ru.practicum.ewm.events.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.ewm.request.model.enums.RequestStatus;

import java.util.List;


@Data
@Builder
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;
    private RequestStatus status;
}