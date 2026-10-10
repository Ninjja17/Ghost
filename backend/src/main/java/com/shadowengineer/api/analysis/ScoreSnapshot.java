package com.shadowengineer.api.analysis;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ScoreSnapshot(
        UUID id,
        UUID userId,
        UUID connectionId,
        int builderScore,
        int finisherScore,
        int learnerScore,
        int architectScore,
        int collaboratorScore,
        String formulaVersion,
        OffsetDateTime observedAt
) {
}
