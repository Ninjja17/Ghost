package com.shadowengineer.api.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Profile("postgres")
public class JdbcSessionStore implements SessionStore {

    private static final Duration SESSION_TTL = Duration.ofHours(24);
    private final JdbcTemplate jdbcTemplate;

    public JdbcSessionStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Session create(UUID userId, String rawToken) {
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plus(SESSION_TTL);
        jdbcTemplate.update("insert into sessions (token_hash, user_id, expires_at) values (?, ?, ?)",
                hash(rawToken), userId, expiresAt);
        return new Session(userId, expiresAt);
    }

    @Override
    public Optional<Session> find(String rawToken) {
        return jdbcTemplate.query("select user_id, expires_at from sessions where token_hash = ? and expires_at > now()",
                        (resultSet, rowNumber) -> new Session(
                                resultSet.getObject("user_id", UUID.class),
                                resultSet.getObject("expires_at", OffsetDateTime.class)),
                        hash(rawToken))
                .stream()
                .findFirst();
    }

    @Override
    public void delete(String rawToken) {
        jdbcTemplate.update("delete from sessions where token_hash = ?", hash(rawToken));
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
