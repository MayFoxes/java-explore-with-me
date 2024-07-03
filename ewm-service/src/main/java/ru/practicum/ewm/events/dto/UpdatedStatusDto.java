package ru.practicum.ewm.events.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UpdatedStatusDto {
    private List<Long> updatedIds;
    private List<Long> processedIds;
}
