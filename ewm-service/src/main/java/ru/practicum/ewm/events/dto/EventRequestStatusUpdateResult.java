package ru.practicum.ewm.events.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import java.util.List;

@Data
@Builder
public class EventRequestStatusUpdateResult {
    private List<ParticipationRequestDto> confirmedRequests;
    private List<ParticipationRequestDto> rejectedRequests;
}