package com.shadowengineer.api.github;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GitHubRepositoryRecord(
        UUID id,
        UUID connectionId,
        String githubRepositoryId,
        String name,
        String htmlUrl,
        String language,
        int stars,
        int forks,
        int size,
        boolean fork,
        OffsetDateTime updatedAt,
        OffsetDateTime observedAt
) {
}
