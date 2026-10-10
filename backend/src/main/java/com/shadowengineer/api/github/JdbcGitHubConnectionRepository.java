package com.shadowengineer.api.github;

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
public class JdbcGitHubConnectionRepository implements GitHubConnectionRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcGitHubConnectionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public GitHubConnection save(GitHubConnection connection) {
        String sql = """
                insert into github_connections (id, user_id, github_user_id, github_username, connected_at, last_synced_at)
                values (?, ?, ?, ?, ?, ?)
                on conflict (user_id) do update set
                    github_user_id = excluded.github_user_id,
                    github_username = excluded.github_username,
                    last_synced_at = excluded.last_synced_at
                """;
        jdbcTemplate.update(sql, connection.id(), connection.userId(), connection.githubUserId(),
                connection.githubUsername(), connection.connectedAt(), connection.lastSyncedAt());
        return findByUserId(connection.userId()).orElse(connection);
    }

    @Override
    public Optional<GitHubConnection> findByUserId(UUID userId) {
        return jdbcTemplate.query("select * from github_connections where user_id = ?", this::mapConnection, userId)
                .stream()
                .findFirst();
    }

    @Override
    public void deleteByUserId(UUID userId) {
        jdbcTemplate.update("delete from github_connections where user_id = ?", userId);
    }

    private GitHubConnection mapConnection(ResultSet resultSet, int rowNumber) throws SQLException {
        return new GitHubConnection(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("user_id", UUID.class),
                resultSet.getLong("github_user_id"),
                resultSet.getString("github_username"),
                resultSet.getObject("connected_at", OffsetDateTime.class),
                resultSet.getObject("last_synced_at", OffsetDateTime.class)
        );
    }
}
