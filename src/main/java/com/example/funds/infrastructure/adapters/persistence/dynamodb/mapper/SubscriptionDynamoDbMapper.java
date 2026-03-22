package com.example.funds.infrastructure.adapters.persistence.dynamodb.mapper;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import com.example.funds.domain.model.Money;
import com.example.funds.domain.model.Subscription;
import com.example.funds.domain.model.SubscriptionStatus;
import com.example.funds.infrastructure.adapters.persistence.dynamodb.document.SubscriptionItem;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public final class SubscriptionDynamoDbMapper {

    private SubscriptionDynamoDbMapper() {
    }

    public static SubscriptionItem toItem(Subscription subscription) {
        String openedAt = subscription.openedAt().toString();
        String status = subscription.status().name();
        return new SubscriptionItem(
                subscription.clientId(),
                subscription.subscriptionId(),
                subscription.fundId(),
                subscription.amount().toLong(),
                status,
                openedAt,
                subscription.cancelledAt() != null ? subscription.cancelledAt().toString() : null,
                status + "#" + openedAt,
                subscription.fundId() + "#" + status
        );
    }

    public static Map<String, AttributeValue> toAttributes(SubscriptionItem item) {
        Map<String, AttributeValue> attributes = new HashMap<>();
        attributes.put("clientId", AttributeValue.fromS(item.clientId()));
        attributes.put("subscriptionId", AttributeValue.fromS(item.subscriptionId()));
        attributes.put("fundId", AttributeValue.fromS(item.fundId()));
        attributes.put("amount", AttributeValue.fromN(String.valueOf(item.amount())));
        attributes.put("status", AttributeValue.fromS(item.status()));
        attributes.put("openedAt", AttributeValue.fromS(item.openedAt()));
        attributes.put("statusOpenedAt", AttributeValue.fromS(item.statusOpenedAt()));
        attributes.put("fundStatus", AttributeValue.fromS(item.fundStatus()));
        if (item.cancelledAt() != null) {
            attributes.put("cancelledAt", AttributeValue.fromS(item.cancelledAt()));
        }
        return attributes;
    }

    public static Subscription toDomain(Map<String, AttributeValue> attributes) {
        return new Subscription(
                attributes.get("subscriptionId").s(),
                attributes.get("clientId").s(),
                attributes.get("fundId").s(),
                Money.of(Long.parseLong(attributes.get("amount").n())),
                SubscriptionStatus.valueOf(attributes.get("status").s()),
                OffsetDateTime.parse(attributes.get("openedAt").s()),
                attributes.containsKey("cancelledAt") ? OffsetDateTime.parse(attributes.get("cancelledAt").s()) : null
        );
    }
}
