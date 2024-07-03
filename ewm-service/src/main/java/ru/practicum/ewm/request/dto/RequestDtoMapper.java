package ru.practicum.ewm.request.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.request.model.Request;

@UtilityClass
public class RequestDtoMapper {
    public ParticipationRequestDto toParticipationRequestDto(Request request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .event(request.getEvent().getId())
                .created(request.getCreated())
                .requester(request.getId())
                .status(request.getStatus())
                .build();
    }
}

