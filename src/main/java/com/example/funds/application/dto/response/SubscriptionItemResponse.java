package com.example.funds.application.dto.response;

import java.time.OffsetDateTime;

import com.example.funds.domain.model.SubscriptionStatus;

public record SubscriptionItemResponse(
        String subscriptionId,
        String fundId,
        long amount,
        SubscriptionStatus status,
        OffsetDateTime openedAt
) {
}
