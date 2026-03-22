package com.example.funds.infrastructure.adapters.persistence.dynamodb.repository;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.example.funds.domain.model.Transaction;
import com.example.funds.domain.model.TransactionType;
import com.example.funds.domain.port.out.TransactionRepositoryPort;
import com.example.funds.infrastructure.adapters.persistence.dynamodb.mapper.TransactionDynamoDbMapper;
import com.example.funds.infrastructure.config.DynamoDbProperties;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

@Repository
public class DynamoDbTransactionRepositoryAdapter implements TransactionRepositoryPort {

    private static final String CLIENT_TYPE_CREATED_AT_INDEX = "client-type-created-at-index";

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public DynamoDbTransactionRepositoryAdapter(DynamoDbClient dynamoDbClient, DynamoDbProperties properties) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = properties.transactionsTable();
    }

    @Override
    public Transaction save(Transaction transaction) {
        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(tableName)
                .item(TransactionDynamoDbMapper.toAttributes(TransactionDynamoDbMapper.toItem(transaction)))
                .build());

        return transaction;
    }

    @Override
    public List<Transaction> findByClientId(String clientId) {
        var response = dynamoDbClient.query(QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("clientId = :clientId")
                .expressionAttributeValues(Map.of(":clientId", AttributeValue.fromS(clientId)))
                .scanIndexForward(false)
                .build());

        return response.items()
                .stream()
                .map(TransactionDynamoDbMapper::toDomain)
                .toList();
    }

    @Override
    public List<Transaction> findByClientIdAndType(String clientId, TransactionType type) {
        var response = dynamoDbClient.query(QueryRequest.builder()
                .tableName(tableName)
                .indexName(CLIENT_TYPE_CREATED_AT_INDEX)
                .keyConditionExpression("clientId = :clientId and begins_with(typeCreatedAt, :typePrefix)")
                .expressionAttributeValues(Map.of(
                        ":clientId", AttributeValue.fromS(clientId),
                        ":typePrefix", AttributeValue.fromS(type.name() + "#")
                ))
                .scanIndexForward(false)
                .build());

        return response.items()
                .stream()
                .map(TransactionDynamoDbMapper::toDomain)
                .toList();
    }
}
