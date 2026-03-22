package com.example.funds.infrastructure.adapters.persistence.dynamodb.document;

import java.util.Set;

public record ClientItem(
        String clientId,
        String fullName,
        String email,
        String phone,
        String passwordHash,
        Set<String> roles,
        Long balance,
        String createdAt
) {
}
