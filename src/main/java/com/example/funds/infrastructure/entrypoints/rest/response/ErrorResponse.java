package com.example.funds.infrastructure.entrypoints.rest.response;

import java.time.OffsetDateTime;
import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        List<String> details,
        String path,
        OffsetDateTime timestamp
) {
}
