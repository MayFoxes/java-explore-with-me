package ru.practicum.ewm.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.EndpointHit;
import ru.practicum.ewm.ViewStats;
import ru.practicum.ewm.mapper.ViewStatMapper;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StatsRepositoryImpl implements StatsRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ViewStatMapper viewStatMapper;

    @Override
    public void saveHit(EndpointHit hit) {
        jdbcTemplate.update("INSERT INTO stats (app, uri, ip, request_time) VALUES (?, ?, ?, ?)",
                hit.getApp(), hit.getUri(), hit.getIp(), hit.getRequestTime());
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        String query = unique ?
                "SELECT app, uri, COUNT (DISTINCT ip) AS hits FROM stats WHERE (request_time >= ? AND request_time <= ?) " :
                "SELECT app, uri, COUNT (ip) AS hits FROM stats WHERE (request_time >= ? AND request_time <= ?) ";

        if (!uris.isEmpty()) {
            query += "AND uri IN ('" + String.join("', '", uris) + "') ";
        }

        query += " GROUP BY app, uri ORDER BY hits DESC";

        return jdbcTemplate.query(query, viewStatMapper, start, end);
    }
}
