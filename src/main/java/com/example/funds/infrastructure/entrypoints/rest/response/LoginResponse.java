package com.example.funds.infrastructure.entrypoints.rest.response;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInMinutes
) {
}
