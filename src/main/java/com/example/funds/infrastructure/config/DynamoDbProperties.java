package com.example.funds.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.aws.dynamodb")
public record DynamoDbProperties(
        String endpoint,
        String region,
        String accessKey,
        String secretKey,
        String clientsTable,
        String fundsTable,
        String subscriptionsTable,
        String transactionsTable
) {
}
