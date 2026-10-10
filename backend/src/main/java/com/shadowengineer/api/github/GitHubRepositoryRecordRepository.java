package com.shadowengineer.api.github;

import java.util.List;
import java.util.UUID;

public interface GitHubRepositoryRecordRepository {

    void replaceSnapshot(UUID connectionId, List<GitHubRepositoryRecord> repositories);

    List<GitHubRepositoryRecord> findByConnectionId(UUID connectionId);
}
