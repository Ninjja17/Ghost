package com.shadowengineer.api.github;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GitHubConnection(
        UUID id,
        UUID userId,
        long githubUserId,
        String githubUsername,
        OffsetDateTime connectedAt,
        OffsetDateTime lastSyncedAt
) {
}
