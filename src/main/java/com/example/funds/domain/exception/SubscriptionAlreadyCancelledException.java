package com.example.funds.domain.exception;

public class SubscriptionAlreadyCancelledException extends BusinessException {

    public SubscriptionAlreadyCancelledException(String subscriptionId) {
        super(
                "SUBSCRIPTION_ALREADY_CANCELLED",
                "Subscription is already cancelled: " + subscriptionId
        );
    }
}
