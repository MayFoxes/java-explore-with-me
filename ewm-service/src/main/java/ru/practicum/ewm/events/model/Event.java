package ru.practicum.ewm.events.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.events.model.enums.EventState;
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
@Entity(name = "EVENTS")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 2000)
    private String annotation;
    @ManyToOne
    @JoinColumn(name = "CATEGORY_ID")
    private Category category;
    @Column(length = 7000)
    private String description;
    private LocalDateTime created;
    @Column(name = "EVENT_DATE")
    private LocalDateTime eventDate;
    @Column(name = "PUBLISHED_DATE")
    private LocalDateTime publishedOn;
    @ManyToOne
    @JoinColumn(name = "LOCATION_ID")
    private Location location;
    @ManyToOne
    @JoinColumn(name = "INITIATOR_ID")
    private User initiator;
    private Boolean paid;
    @Column(name = "PARTICIPANT_LIMIT")
    private Integer participantLimit;
    @Column(name = "REQUEST_MODERATION")
    private Boolean requestModeration;
    @Column(nullable = false, length = 120)
    private String title;
    @Enumerated(EnumType.STRING)
    private EventState state;
}
