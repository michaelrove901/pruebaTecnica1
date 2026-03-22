package com.example.funds.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.example.funds.domain.model.Subscription;
import com.example.funds.domain.model.SubscriptionStatus;

public interface SubscriptionRepositoryPort {

    Subscription save(Subscription subscription);

    Optional<Subscription> findById(String subscriptionId);

    Optional<Subscription> findActiveByClientIdAndFundId(String clientId, String fundId);

    List<Subscription> findByClientId(String clientId);

    List<Subscription> findByClientIdAndStatus(String clientId, SubscriptionStatus status);
}
