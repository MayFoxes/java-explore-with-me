package ru.practicum.ewm.events.model.enums;

import java.util.Optional;

public enum UpdateEventState {
    SEND_TO_REVIEW,
    CANCEL_REVIEW,
    PUBLISH_EVENT,
    REJECT_EVENT;

    public static Optional<UpdateEventState> from(String stringState) {
        for (UpdateEventState state : values()) {
            if (state.name().equalsIgnoreCase(stringState)) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }
}
