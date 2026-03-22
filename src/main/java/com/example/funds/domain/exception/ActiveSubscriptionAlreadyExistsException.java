package com.example.funds.domain.exception;

public class ActiveSubscriptionAlreadyExistsException extends BusinessException {

    public ActiveSubscriptionAlreadyExistsException(String clientId, String fundId) {
        super(
                "ACTIVE_SUBSCRIPTION_ALREADY_EXISTS",
                "Client " + clientId + " already has an active subscription for fund " + fundId
        );
    }
}
