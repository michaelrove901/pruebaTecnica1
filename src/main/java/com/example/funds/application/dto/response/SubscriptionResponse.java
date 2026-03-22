package com.example.funds.application.dto.response;

import java.time.OffsetDateTime;

import com.example.funds.domain.model.NotificationPreference;
import com.example.funds.domain.model.SubscriptionStatus;

public record SubscriptionResponse(
        String subscriptionId,
        String clientId,
        String fundId,
        long amount,
        SubscriptionStatus status,
        NotificationPreference notificationPreference,
        String transactionId,
        long currentBalance,
        OffsetDateTime openedAt,
        OffsetDateTime cancelledAt
) {
}
