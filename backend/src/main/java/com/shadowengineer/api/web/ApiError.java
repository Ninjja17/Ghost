package com.shadowengineer.api.web;

import java.time.OffsetDateTime;

public record ApiError(
        String code,
        String message,
        String path,
        String requestId,
        OffsetDateTime timestamp
) {
}
