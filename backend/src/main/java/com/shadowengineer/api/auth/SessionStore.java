package com.shadowengineer.api.auth;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface SessionStore {

    Session create(UUID userId, String rawToken);

    Optional<Session> find(String rawToken);

    void delete(String rawToken);

    record Session(UUID userId, OffsetDateTime expiresAt) {
    }
}
