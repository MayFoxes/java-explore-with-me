package ru.practicum.ewm.events.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.utility.Pagination;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    List<Event> findAllByInitiatorId(Long id, Pagination page);

    Optional<Event> findByInitiatorIdAndId(Long userId, Long eventId);

    List<Event> findAllByIdIn(List<Long> ids);
}
