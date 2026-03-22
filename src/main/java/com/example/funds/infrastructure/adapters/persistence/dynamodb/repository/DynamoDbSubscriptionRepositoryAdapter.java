package com.example.funds.infrastructure.adapters.persistence.dynamodb.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.funds.domain.model.Subscription;
import com.example.funds.domain.model.SubscriptionStatus;
import com.example.funds.domain.port.out.SubscriptionRepositoryPort;
import com.example.funds.infrastructure.adapters.persistence.dynamodb.mapper.SubscriptionDynamoDbMapper;
import com.example.funds.infrastructure.config.DynamoDbProperties;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

@Repository
public class DynamoDbSubscriptionRepositoryAdapter implements SubscriptionRepositoryPort {

    private static final String SUBSCRIPTION_ID_INDEX = "subscription-id-index";
    private static final String CLIENT_STATUS_INDEX = "client-status-index";
    private static final String CLIENT_FUND_STATUS_INDEX = "client-fund-status-index";

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public DynamoDbSubscriptionRepositoryAdapter(DynamoDbClient dynamoDbClient, DynamoDbProperties properties) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = properties.subscriptionsTable();
    }

    @Override
    public Subscription save(Subscription subscription) {
        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(tableName)
                .item(SubscriptionDynamoDbMapper.toAttributes(SubscriptionDynamoDbMapper.toItem(subscription)))
                .build());

        return subscription;
    }

    @Override
    public Optional<Subscription> findById(String subscriptionId) {
        var response = dynamoDbClient.query(QueryRequest.builder()
                .tableName(tableName)
                .indexName(SUBSCRIPTION_ID_INDEX)
                .keyConditionExpression("subscriptionId = :subscriptionId")
                .expressionAttributeValues(Map.of(":subscriptionId", AttributeValue.fromS(subscriptionId)))
                .limit(1)
                .build());

        if (!response.hasItems() || response.items().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(SubscriptionDynamoDbMapper.toDomain(response.items().getFirst()));
    }

    @Override
    public Optional<Subscription> findActiveByClientIdAndFundId(String clientId, String fundId) {
        var response = dynamoDbClient.query(QueryRequest.builder()
                .tableName(tableName)
                .indexName(CLIENT_FUND_STATUS_INDEX)
                .keyConditionExpression("clientId = :clientId and fundStatus = :fundStatus")
                .expressionAttributeValues(Map.of(
                        ":clientId", AttributeValue.fromS(clientId),
                        ":fundStatus", AttributeValue.fromS(fundId + "#" + SubscriptionStatus.ACTIVE.name())
                ))
                .limit(1)
                .build());

        if (!response.hasItems() || response.items().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(SubscriptionDynamoDbMapper.toDomain(response.items().getFirst()));
    }

    @Override
    public List<Subscription> findByClientId(String clientId) {
        var response = dynamoDbClient.query(QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("clientId = :clientId")
                .expressionAttributeValues(Map.of(":clientId", AttributeValue.fromS(clientId)))
                .build());

        return response.items()
                .stream()
                .map(SubscriptionDynamoDbMapper::toDomain)
                .toList();
    }

    @Override
    public List<Subscription> findByClientIdAndStatus(String clientId, SubscriptionStatus status) {
        var response = dynamoDbClient.query(QueryRequest.builder()
                .tableName(tableName)
                .indexName(CLIENT_STATUS_INDEX)
                .keyConditionExpression("clientId = :clientId and begins_with(statusOpenedAt, :status)")
                .expressionAttributeValues(Map.of(
                        ":clientId", AttributeValue.fromS(clientId),
                        ":status", AttributeValue.fromS(status.name() + "#")
                ))
                .scanIndexForward(false)
                .build());

        return response.items()
                .stream()
                .map(SubscriptionDynamoDbMapper::toDomain)
                .toList();
    }
}
