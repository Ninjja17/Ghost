package com.shadowengineer.api.web;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

    private final HealthEndpoint healthEndpoint;
    private final String applicationVersion;

    public HealthController(HealthEndpoint healthEndpoint,
                             @Value("${app.version:0.1.0-SNAPSHOT}") String applicationVersion) {
        this.healthEndpoint = healthEndpoint;
        this.applicationVersion = applicationVersion;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", "shadow-engineer-api",
                "version", applicationVersion,
                "timestamp", OffsetDateTime.now(ZoneOffset.UTC)
        );
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        String status = healthEndpoint.health().getStatus().getCode();
        Map<String, Object> body = Map.of(
                "status", status,
                "service", "shadow-engineer-api",
                "version", applicationVersion,
                "timestamp", OffsetDateTime.now(ZoneOffset.UTC)
        );
        return "UP".equals(status) ? ResponseEntity.ok(body) : ResponseEntity.status(503).body(body);
    }
}
