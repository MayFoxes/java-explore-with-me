package ru.practicum.ewm.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.ewm.EndpointHit;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class StatsClient extends BaseClient {

    public StatsClient(@Value("${stat-service.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> saveHit(EndpointHit hit) {
        return post(hit);
    }

    public ResponseEntity<Object> getHit(String start, String end, @Nullable List<String> uris, Boolean unique) {
        String uri = "/stats?start={start}&end={end}&unique={unique}&uris={uris}";
        Map<String, Object> parameters = Map.of(
                "start", encode(start),
                "end", encode(end),
                "unique", unique,
                "uris", uris != null ? String.join(",", uris) : null
        );
        return get(uri, parameters);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}