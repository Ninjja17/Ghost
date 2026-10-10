package com.shadowengineer.api.interpretation;

import java.util.List;
import java.util.UUID;

public record MentorInsight(
        String mentorType,
        String lens,
        String finding,
        String recommendation,
        String question,
        int confidence,
        String uncertainty,
        List<UUID> evidenceIds
) {
}
