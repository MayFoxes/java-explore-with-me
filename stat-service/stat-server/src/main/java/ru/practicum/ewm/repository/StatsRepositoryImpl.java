package ru.practicum.ewm.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.Constant;
import ru.practicum.ewm.EndpointHit;
import ru.practicum.ewm.ViewStats;
import ru.practicum.ewm.mapper.ViewStatMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StatsRepositoryImpl implements StatsRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ViewStatMapper viewStatMapper;

    @Override
    public void saveHit(EndpointHit hit) {
        LocalDateTime dateTime = LocalDateTime.parse(hit.getTimestamp(), DateTimeFormatter.ofPattern(Constant.DATE_FORMAT));

        jdbcTemplate.update("INSERT INTO stats (app, uri, ip, request_time) VALUES (?, ?, ?, ?)",
                hit.getApp(), hit.getUri(), hit.getIp(), dateTime);
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        StringBuilder query = new StringBuilder(unique ?
                "SELECT app, uri, COUNT (DISTINCT ip) AS hits FROM stats WHERE (request_time >= ? AND request_time <= ?) " :
                "SELECT app, uri, COUNT (ip) AS hits FROM stats WHERE (request_time >= ? AND request_time <= ?) ");

        if (!uris.isEmpty()) {
            query.append("AND uri IN ('").append(String.join("', '", uris)).append("') ");
        }

        query.append(" GROUP BY app, uri ORDER BY hits DESC");

        return jdbcTemplate.query(query.toString(), viewStatMapper, start, end);
    }
}
