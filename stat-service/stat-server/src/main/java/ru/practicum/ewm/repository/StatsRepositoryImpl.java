package ru.practicum.ewm.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.EndpointHit;
import ru.practicum.ewm.ViewStats;
import ru.practicum.ewm.mapper.ViewStatMapper;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StatsRepositoryImpl implements StatsRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ViewStatMapper viewStatMapper;

    @Override
    public void saveHit(EndpointHit hit) {
        jdbcTemplate.update("INSERT INTO stats (app, uri, ip, created) VALUES (?, ?, ?, ?)",
                hit.getApp(), hit.getUri(), hit.getIp(), Timestamp.valueOf(hit.getTimestamp()));
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        StringBuilder query = new StringBuilder(unique ?
                "SELECT app, uri, COUNT (DISTINCT ip) AS hits FROM stats WHERE (created >= ? AND created <= ?) " :
                "SELECT app, uri, COUNT (ip) AS hits FROM stats WHERE (created >= ? AND created <= ?) ");

        if (!uris.isEmpty()) {
            query.append("AND uri IN ('").append(String.join("', '", uris)).append("') ");
        }

        query.append(" GROUP BY app, uri ORDER BY hits DESC");

        return jdbcTemplate.query(query.toString(), viewStatMapper, start, end);
    }
}
