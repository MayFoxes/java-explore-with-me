package ru.practicum.ewm;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ViewStats {
    private final String app;
    private final String uri;
    private final Integer hits;
}