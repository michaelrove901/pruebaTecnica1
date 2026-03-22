package com.example.funds.domain.model;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public record Transaction(
        String transactionId,
        String clientId,
        String fundId,
        String subscriptionId,
        TransactionType type,
        Money amount,
        String message,
        NotificationPreference notificationPreference,
        OffsetDateTime createdAt
) {

    public Transaction {
        Objects.requireNonNull(transactionId, "transactionId is required");
        Objects.requireNonNull(clientId, "clientId is required");
        Objects.requireNonNull(fundId, "fundId is required");
        Objects.requireNonNull(type, "type is required");
        Objects.requireNonNull(amount, "amount is required");
        Objects.requireNonNull(createdAt, "createdAt is required");
        if (amount.toLong() <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
        if (subscriptionId == null || subscriptionId.isBlank()) {
            throw new IllegalArgumentException("subscriptionId is required");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message is required");
        }
    }

    public static Transaction subscription(
            String clientId,
            String fundId,
            String subscriptionId,
            Money amount,
            NotificationPreference notificationPreference
    ) {
        return new Transaction(
                UUID.randomUUID().toString(),
                clientId,
                fundId,
                subscriptionId,
                TransactionType.SUBSCRIPTION,
                amount,
                "Subscription completed",
                notificationPreference,
                OffsetDateTime.now()
        );
    }

    public static Transaction cancellation(String clientId, String fundId, String subscriptionId, Money amount) {
        return new Transaction(
                UUID.randomUUID().toString(),
                clientId,
                fundId,
                subscriptionId,
                TransactionType.CANCELLATION,
                amount,
                "Cancellation completed",
                null,
                OffsetDateTime.now()
        );
    }
}
