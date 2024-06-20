package ru.practicum.ewm;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RequestedViewStats {
    private final String app;
    private final List<String> uris;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private boolean unique;
}