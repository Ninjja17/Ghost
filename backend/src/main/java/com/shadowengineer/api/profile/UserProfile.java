package com.shadowengineer.api.profile;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserProfile(
        UUID id,
        String name,
        String email,
        String targetRole,
        String experienceLevel,
        String growthGoal,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
