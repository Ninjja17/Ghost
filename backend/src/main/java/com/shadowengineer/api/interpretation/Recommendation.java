package com.shadowengineer.api.interpretation;

import java.util.List;
import java.util.UUID;

public record Recommendation(
        String category,
        String title,
        String action,
        String rationale,
        int confidence,
        List<UUID> evidenceIds
) {
}
