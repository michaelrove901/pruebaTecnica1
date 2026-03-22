package com.example.funds.domain.exception;

public class SubscriptionNotFoundException extends ResourceNotFoundException {

    public SubscriptionNotFoundException(String subscriptionId) {
        super("SUBSCRIPTION_NOT_FOUND", "Subscription not found: " + subscriptionId);
    }
}
