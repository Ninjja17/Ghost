package com.shadowengineer.api.github;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!postgres")
public class InMemoryGitHubRepositoryRecordRepository implements GitHubRepositoryRecordRepository {

    private final Map<UUID, List<GitHubRepositoryRecord>> snapshots = new ConcurrentHashMap<>();

    @Override
    public void replaceSnapshot(UUID connectionId, List<GitHubRepositoryRecord> repositories) {
        snapshots.put(connectionId, List.copyOf(repositories));
    }

    @Override
    public List<GitHubRepositoryRecord> findByConnectionId(UUID connectionId) {
        return new ArrayList<>(snapshots.getOrDefault(connectionId, List.of()));
    }
}
