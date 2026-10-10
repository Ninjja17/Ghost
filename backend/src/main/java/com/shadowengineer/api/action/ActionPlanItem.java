package com.shadowengineer.api.action;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ActionPlanItem(
        UUID id,
        UUID userId,
        int weekNumber,
        String title,
        String details,
        String completionCriteria,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
