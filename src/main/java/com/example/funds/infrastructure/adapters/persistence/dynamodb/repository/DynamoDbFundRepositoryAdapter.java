package com.example.funds.infrastructure.adapters.persistence.dynamodb.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.funds.domain.model.Fund;
import com.example.funds.domain.port.out.FundRepositoryPort;
import com.example.funds.infrastructure.adapters.persistence.dynamodb.mapper.FundDynamoDbMapper;
import com.example.funds.infrastructure.config.DynamoDbProperties;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

@Repository
public class DynamoDbFundRepositoryAdapter implements FundRepositoryPort {

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public DynamoDbFundRepositoryAdapter(DynamoDbClient dynamoDbClient, DynamoDbProperties properties) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = properties.fundsTable();
    }

    @Override
    public Optional<Fund> findById(String fundId) {
        var response = dynamoDbClient.getItem(GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("fundId", AttributeValue.fromS(fundId)))
                .build());

        if (!response.hasItem() || response.item().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(FundDynamoDbMapper.toDomain(response.item()));
    }

    @Override
    public List<Fund> findAllActive() {
        var response = dynamoDbClient.scan(ScanRequest.builder()
                .tableName(tableName)
                .filterExpression("active = :active")
                .expressionAttributeValues(Map.of(":active", AttributeValue.fromBool(true)))
                .build());

        return response.items()
                .stream()
                .map(FundDynamoDbMapper::toDomain)
                .toList();
    }
}
