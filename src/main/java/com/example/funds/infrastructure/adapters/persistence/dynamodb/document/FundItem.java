package com.example.funds.infrastructure.adapters.persistence.dynamodb.document;

public record FundItem(
        String fundId,
        String name,
        String category,
        Long minimumAmount,
        Boolean active
) {
}
