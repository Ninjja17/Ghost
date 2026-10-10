package com.shadowengineer.api.auth;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Profile("postgres")
public class JdbcAuthUserRepository implements AuthUserRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAuthUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AuthUser save(AuthUser user) {
        jdbcTemplate.update("insert into users (id, email, password_hash) values (?, ?, ?)",
                user.id(), user.email(), user.passwordHash());
        return user;
    }

    @Override
    public Optional<AuthUser> findByEmail(String email) {
        return jdbcTemplate.query("select id, email, password_hash, created_at from users where lower(email) = lower(?)",
                        this::mapUser, email)
                .stream()
                .findFirst();
    }

    @Override
    public Optional<AuthUser> findById(UUID id) {
        return jdbcTemplate.query("select id, email, password_hash, created_at from users where id = ?",
                        this::mapUser, id)
                .stream()
                .findFirst();
    }

    private AuthUser mapUser(ResultSet resultSet, int rowNumber) throws SQLException {
        return new AuthUser(
                resultSet.getObject("id", UUID.class),
                resultSet.getString("email"),
                resultSet.getString("password_hash"),
                resultSet.getObject("created_at", OffsetDateTime.class)
        );
    }
}
