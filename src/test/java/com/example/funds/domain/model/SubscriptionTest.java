package com.example.funds.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.example.funds.domain.exception.SubscriptionAlreadyCancelledException;

class SubscriptionTest {

    @Test
    void shouldCancelActiveSubscription() {
        Subscription subscription = Subscription.create("sub-1", "client-1", "fund-1", Money.of(75000));

        subscription.cancel();

        assertThat(subscription.status()).isEqualTo(SubscriptionStatus.CANCELLED);
        assertThat(subscription.cancelledAt()).isNotNull();
    }

    @Test
    void shouldRejectCancellationWhenSubscriptionIsAlreadyCancelled() {
        Subscription subscription = new Subscription(
                "sub-1",
                "client-1",
                "fund-1",
                Money.of(75000),
                SubscriptionStatus.CANCELLED,
                java.time.OffsetDateTime.now().minusDays(1),
                java.time.OffsetDateTime.now()
        );

        assertThatThrownBy(subscription::cancel)
                .isInstanceOf(SubscriptionAlreadyCancelledException.class)
                .hasMessage("Subscription is already cancelled: sub-1");
    }
}
