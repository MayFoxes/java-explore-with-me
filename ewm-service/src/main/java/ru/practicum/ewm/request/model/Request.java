package ru.practicum.ewm.request.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.request.model.enums.RequestStatus;
import ru.practicum.ewm.users.model.User;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "REQUESTS")
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDateTime created;
    @ManyToOne
    @JoinColumn(name = "EVENT_ID")
    private Event event;
    @ManyToOne
    @JoinColumn(name = "REQUESTER_ID")
    private User requester;
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private RequestStatus status;
}