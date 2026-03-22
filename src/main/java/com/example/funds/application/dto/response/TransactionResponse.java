package com.example.funds.application.dto.response;

import java.time.OffsetDateTime;

import com.example.funds.domain.model.NotificationPreference;
import com.example.funds.domain.model.TransactionType;

public record TransactionResponse(
        String transactionId,
        String clientId,
        String fundId,
        String subscriptionId,
        TransactionType type,
        long amount,
        String message,
        NotificationPreference notificationPreference,
        OffsetDateTime createdAt
) {
}
