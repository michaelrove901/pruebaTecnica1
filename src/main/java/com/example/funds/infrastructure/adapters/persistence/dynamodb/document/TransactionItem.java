package com.example.funds.infrastructure.adapters.persistence.dynamodb.document;

public record TransactionItem(
        String clientId,
        String createdAtTransactionId,
        String transactionId,
        String fundId,
        String subscriptionId,
        String type,
        Long amount,
        String message,
        String notificationPreference,
        String createdAt,
        String typeCreatedAt
) {
}
