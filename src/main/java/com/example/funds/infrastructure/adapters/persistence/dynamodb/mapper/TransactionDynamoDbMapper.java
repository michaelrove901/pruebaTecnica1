package com.example.funds.infrastructure.adapters.persistence.dynamodb.mapper;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import com.example.funds.domain.model.Money;
import com.example.funds.domain.model.NotificationPreference;
import com.example.funds.domain.model.Transaction;
import com.example.funds.domain.model.TransactionType;
import com.example.funds.infrastructure.adapters.persistence.dynamodb.document.TransactionItem;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public final class TransactionDynamoDbMapper {

    private TransactionDynamoDbMapper() {
    }

    public static TransactionItem toItem(Transaction transaction) {
        String createdAt = transaction.createdAt().toString();
        return new TransactionItem(
                transaction.clientId(),
                createdAt + "#" + transaction.transactionId(),
                transaction.transactionId(),
                transaction.fundId(),
                transaction.subscriptionId(),
                transaction.type().name(),
                transaction.amount().toLong(),
                transaction.message(),
                transaction.notificationPreference() != null ? transaction.notificationPreference().name() : null,
                createdAt,
                transaction.type().name() + "#" + createdAt
        );
    }

    public static Map<String, AttributeValue> toAttributes(TransactionItem item) {
        Map<String, AttributeValue> attributes = new HashMap<>();
        attributes.put("clientId", AttributeValue.fromS(item.clientId()));
        attributes.put("createdAtTransactionId", AttributeValue.fromS(item.createdAtTransactionId()));
        attributes.put("transactionId", AttributeValue.fromS(item.transactionId()));
        attributes.put("fundId", AttributeValue.fromS(item.fundId()));
        attributes.put("subscriptionId", AttributeValue.fromS(item.subscriptionId()));
        attributes.put("type", AttributeValue.fromS(item.type()));
        attributes.put("amount", AttributeValue.fromN(String.valueOf(item.amount())));
        attributes.put("message", AttributeValue.fromS(item.message()));
        attributes.put("createdAt", AttributeValue.fromS(item.createdAt()));
        attributes.put("typeCreatedAt", AttributeValue.fromS(item.typeCreatedAt()));
        if (item.notificationPreference() != null) {
            attributes.put("notificationPreference", AttributeValue.fromS(item.notificationPreference()));
        }
        return attributes;
    }

    public static Transaction toDomain(Map<String, AttributeValue> attributes) {
        return new Transaction(
                attributes.get("transactionId").s(),
                attributes.get("clientId").s(),
                attributes.get("fundId").s(),
                attributes.get("subscriptionId").s(),
                TransactionType.valueOf(attributes.get("type").s()),
                Money.of(Long.parseLong(attributes.get("amount").n())),
                attributes.get("message").s(),
                attributes.containsKey("notificationPreference")
                        ? NotificationPreference.valueOf(attributes.get("notificationPreference").s())
                        : null,
                OffsetDateTime.parse(attributes.get("createdAt").s())
        );
    }
}
