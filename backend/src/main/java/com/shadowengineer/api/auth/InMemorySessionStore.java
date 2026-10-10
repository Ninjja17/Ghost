package com.shadowengineer.api.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!postgres")
public class InMemorySessionStore implements SessionStore {

    private static final Duration SESSION_TTL = Duration.ofHours(24);
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    @Override
    public Session create(UUID userId, String rawToken) {
        Session session = new Session(userId, OffsetDateTime.now(ZoneOffset.UTC).plus(SESSION_TTL));
        sessions.put(hash(rawToken), session);
        return session;
    }

    @Override
    public Optional<Session> find(String rawToken) {
        Session session = sessions.get(hash(rawToken));
        if (session == null) return Optional.empty();
        if (session.expiresAt().isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
            sessions.remove(hash(rawToken));
            return Optional.empty();
        }
        return Optional.of(session);
    }

    @Override
    public void delete(String rawToken) {
        sessions.remove(hash(rawToken));
    }

    private String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
