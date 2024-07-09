package ru.practicum.ewm.events.model.enums;

import java.util.Arrays;
import java.util.Optional;

public enum EventUserState {
    SEND_TO_REVIEW,
    CANCEL_REVIEW;

    public static Optional<EventUserState> from(String stringState) {
        return Arrays.stream(values())
                .filter(s -> s.name().equalsIgnoreCase(stringState))
                .findFirst();
    }
}
