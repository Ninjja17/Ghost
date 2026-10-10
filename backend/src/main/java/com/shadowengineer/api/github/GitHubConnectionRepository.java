package com.shadowengineer.api.github;

import java.util.Optional;
import java.util.UUID;

public interface GitHubConnectionRepository {

    GitHubConnection save(GitHubConnection connection);

    Optional<GitHubConnection> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
