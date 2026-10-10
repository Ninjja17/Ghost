package com.shadowengineer.api.evidence;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EvidenceRecord(
        UUID id,
        UUID userId,
        UUID connectionId,
        String evidenceType,
        String claim,
        String value,
        int confidence,
        String extractorVersion,
        OffsetDateTime observedAt
) {
}
