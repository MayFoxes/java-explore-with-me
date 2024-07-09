package ru.practicum.ewm.events.model.enums;

import java.util.Arrays;
import java.util.Optional;

public enum EventAdminState {
    PUBLISH_EVENT,
    REJECT_EVENT;

    public static Optional<EventAdminState> from(String stringState) {
        return Arrays.stream(values())
                .filter(s -> s.name().equalsIgnoreCase(stringState))
                .findFirst();
    }
}
