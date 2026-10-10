package com.shadowengineer.api.auth;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuthUser(
        UUID id,
        String email,
        String passwordHash,
        OffsetDateTime createdAt
) {
}
