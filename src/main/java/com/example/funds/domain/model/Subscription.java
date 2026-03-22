package com.example.funds.domain.model;

import java.time.OffsetDateTime;
import java.util.Objects;

import com.example.funds.domain.exception.SubscriptionAlreadyCancelledException;

public class Subscription {

    private final String subscriptionId;
    private final String clientId;
    private final String fundId;
    private final Money amount;
    private final OffsetDateTime openedAt;
    private SubscriptionStatus status;
    private OffsetDateTime cancelledAt;

    public Subscription(
            String subscriptionId,
            String clientId,
            String fundId,
            Money amount,
            SubscriptionStatus status,
            OffsetDateTime openedAt,
            OffsetDateTime cancelledAt
    ) {
        this.subscriptionId = Objects.requireNonNull(subscriptionId, "subscriptionId is required");
        this.clientId = Objects.requireNonNull(clientId, "clientId is required");
        this.fundId = Objects.requireNonNull(fundId, "fundId is required");
        this.amount = Objects.requireNonNull(amount, "amount is required");
        this.status = Objects.requireNonNullElse(status, SubscriptionStatus.ACTIVE);
        this.openedAt = Objects.requireNonNullElseGet(openedAt, OffsetDateTime::now);
        this.cancelledAt = cancelledAt;
        if (amount.toLong() <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
        if (SubscriptionStatus.CANCELLED.equals(this.status) && this.cancelledAt == null) {
            throw new IllegalArgumentException("cancelledAt is required when subscription is cancelled");
        }
        if (SubscriptionStatus.ACTIVE.equals(this.status) && this.cancelledAt != null) {
            throw new IllegalArgumentException("cancelledAt must be null when subscription is active");
        }
    }

    public static Subscription create(String subscriptionId, String clientId, String fundId, Money amount) {
        return new Subscription(
                subscriptionId,
                clientId,
                fundId,
                amount,
                SubscriptionStatus.ACTIVE,
                OffsetDateTime.now(),
                null
        );
    }

    public void cancel() {
        if (!isActive()) {
            throw new SubscriptionAlreadyCancelledException(subscriptionId);
        }
        this.status = SubscriptionStatus.CANCELLED;
        this.cancelledAt = OffsetDateTime.now();
    }

    public boolean isActive() {
        return SubscriptionStatus.ACTIVE.equals(status);
    }

    public String subscriptionId() {
        return subscriptionId;
    }

    public String clientId() {
        return clientId;
    }

    public String fundId() {
        return fundId;
    }

    public Money amount() {
        return amount;
    }

    public SubscriptionStatus status() {
        return status;
    }

    public OffsetDateTime openedAt() {
        return openedAt;
    }

    public OffsetDateTime cancelledAt() {
        return cancelledAt;
    }
}
