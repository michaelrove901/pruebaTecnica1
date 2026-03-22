package com.example.funds.infrastructure.adapters.persistence.dynamodb.repository;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.funds.domain.model.Client;
import com.example.funds.domain.port.out.ClientRepositoryPort;
import com.example.funds.infrastructure.adapters.persistence.dynamodb.mapper.ClientDynamoDbMapper;
import com.example.funds.infrastructure.config.DynamoDbProperties;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

@Repository
public class DynamoDbClientRepositoryAdapter implements ClientRepositoryPort {

    private static final String EMAIL_INDEX = "email-index";

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public DynamoDbClientRepositoryAdapter(DynamoDbClient dynamoDbClient, DynamoDbProperties properties) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = properties.clientsTable();
    }

    @Override
    public Optional<Client> findById(String clientId) {
        var response = dynamoDbClient.getItem(GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("clientId", AttributeValue.fromS(clientId)))
                .build());

        if (!response.hasItem() || response.item().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(ClientDynamoDbMapper.toDomain(response.item()));
    }

    @Override
    public Optional<Client> findByEmail(String email) {
        var response = dynamoDbClient.query(QueryRequest.builder()
                .tableName(tableName)
                .indexName(EMAIL_INDEX)
                .keyConditionExpression("email = :email")
                .expressionAttributeValues(Map.of(":email", AttributeValue.fromS(email)))
                .limit(1)
                .build());

        if (!response.hasItems() || response.items().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(ClientDynamoDbMapper.toDomain(response.items().getFirst()));
    }

    @Override
    public Client save(Client client) {
        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(tableName)
                .item(ClientDynamoDbMapper.toAttributes(ClientDynamoDbMapper.toItem(client)))
                .build());

        return client;
    }
}
