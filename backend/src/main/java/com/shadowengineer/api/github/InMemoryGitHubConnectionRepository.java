package com.shadowengineer.api.github;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!postgres")
public class InMemoryGitHubConnectionRepository implements GitHubConnectionRepository {

    private final Map<UUID, GitHubConnection> connections = new ConcurrentHashMap<>();

    @Override
    public GitHubConnection save(GitHubConnection connection) {
        connections.put(connection.userId(), connection);
        return connection;
    }

    @Override
    public Optional<GitHubConnection> findByUserId(UUID userId) {
        return Optional.ofNullable(connections.get(userId));
    }

    @Override
    public void deleteByUserId(UUID userId) {
        connections.remove(userId);
    }
}
