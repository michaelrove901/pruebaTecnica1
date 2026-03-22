package com.example.funds.infrastructure.entrypoints.rest.response;

import java.time.OffsetDateTime;

import com.example.funds.domain.model.SubscriptionStatus;

public record SubscriptionItemRestResponse(
        String subscriptionId,
        String fundId,
        long amount,
        SubscriptionStatus status,
        OffsetDateTime openedAt
) {
}
