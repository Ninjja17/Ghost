package com.shadowengineer.api.github;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Profile("postgres")
public class JdbcGitHubRepositoryRecordRepository implements GitHubRepositoryRecordRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcGitHubRepositoryRecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void replaceSnapshot(UUID connectionId, List<GitHubRepositoryRecord> repositories) {
        jdbcTemplate.update("delete from github_repositories where connection_id = ?", connectionId);
        String sql = """
                insert into github_repositories
                    (id, connection_id, github_repository_id, name, html_url, language, stars, forks, size, is_fork, updated_at, observed_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.batchUpdate(sql, repositories, repositories.size(), (statement, repository) -> {
            statement.setObject(1, repository.id());
            statement.setObject(2, repository.connectionId());
            statement.setString(3, repository.githubRepositoryId());
            statement.setString(4, repository.name());
            statement.setString(5, repository.htmlUrl());
            statement.setString(6, repository.language());
            statement.setInt(7, repository.stars());
            statement.setInt(8, repository.forks());
            statement.setInt(9, repository.size());
            statement.setBoolean(10, repository.fork());
            statement.setObject(11, repository.updatedAt());
            statement.setObject(12, repository.observedAt());
        });
    }

    @Override
    public List<GitHubRepositoryRecord> findByConnectionId(UUID connectionId) {
        return jdbcTemplate.query("select * from github_repositories where connection_id = ? order by updated_at desc",
                this::mapRepository, connectionId);
    }

    private GitHubRepositoryRecord mapRepository(ResultSet resultSet, int rowNumber) throws SQLException {
        return new GitHubRepositoryRecord(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("connection_id", UUID.class),
                resultSet.getString("github_repository_id"),
                resultSet.getString("name"),
                resultSet.getString("html_url"),
                resultSet.getString("language"),
                resultSet.getInt("stars"),
                resultSet.getInt("forks"),
                resultSet.getInt("size"),
                resultSet.getBoolean("is_fork"),
                resultSet.getObject("updated_at", OffsetDateTime.class),
                resultSet.getObject("observed_at", OffsetDateTime.class)
        );
    }
}
