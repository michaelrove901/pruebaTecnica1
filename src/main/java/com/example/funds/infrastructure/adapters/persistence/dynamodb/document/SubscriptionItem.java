package com.example.funds.infrastructure.adapters.persistence.dynamodb.document;

public record SubscriptionItem(
        String clientId,
        String subscriptionId,
        String fundId,
        Long amount,
        String status,
        String openedAt,
        String cancelledAt,
        String statusOpenedAt,
        String fundStatus
) {
}
