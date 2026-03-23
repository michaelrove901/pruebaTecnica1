package com.example.funds.infrastructure.config;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.Projection;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.TableStatus;

@Component
@Profile("local")
public class LocalDynamoDbInitializer implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalDynamoDbInitializer.class);

    private static final String EMAIL_INDEX = "email-index";
    private static final String SUBSCRIPTION_ID_INDEX = "subscription-id-index";
    private static final String CLIENT_STATUS_INDEX = "client-status-index";
    private static final String CLIENT_FUND_STATUS_INDEX = "client-fund-status-index";
    private static final String CLIENT_TYPE_CREATED_AT_INDEX = "client-type-created-at-index";

    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbProperties properties;
    private final DemoDataSeeder demoDataSeeder;

    public LocalDynamoDbInitializer(
            DynamoDbClient dynamoDbClient,
            DynamoDbProperties properties,
            DemoDataSeeder demoDataSeeder
    ) {
        this.dynamoDbClient = dynamoDbClient;
        this.properties = properties;
        this.demoDataSeeder = demoDataSeeder;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        createClientsTableIfNeeded();
        createFundsTableIfNeeded();
        createSubscriptionsTableIfNeeded();
        createTransactionsTableIfNeeded();

        demoDataSeeder.seedDemoDataIfMissing();
        demoDataSeeder.logDemoCredentials();
        LOGGER.info("Local DynamoDB initialization completed.");
    }

    private void createClientsTableIfNeeded() throws InterruptedException {
        if (tableExists(properties.clientsTable())) {
            return;
        }

        dynamoDbClient.createTable(CreateTableRequest.builder()
                .tableName(properties.clientsTable())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .attributeDefinitions(
                        AttributeDefinition.builder().attributeName("clientId").attributeType(ScalarAttributeType.S).build(),
                        AttributeDefinition.builder().attributeName("email").attributeType(ScalarAttributeType.S).build()
                )
                .keySchema(KeySchemaElement.builder().attributeName("clientId").keyType(KeyType.HASH).build())
                .globalSecondaryIndexes(
                        GlobalSecondaryIndex.builder()
                                .indexName(EMAIL_INDEX)
                                .keySchema(KeySchemaElement.builder().attributeName("email").keyType(KeyType.HASH).build())
                                .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                                .build()
                )
                .build());

        waitForTable(properties.clientsTable());
    }

    private void createFundsTableIfNeeded() throws InterruptedException {
        if (tableExists(properties.fundsTable())) {
            return;
        }

        dynamoDbClient.createTable(CreateTableRequest.builder()
                .tableName(properties.fundsTable())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .attributeDefinitions(
                        AttributeDefinition.builder().attributeName("fundId").attributeType(ScalarAttributeType.S).build()
                )
                .keySchema(KeySchemaElement.builder().attributeName("fundId").keyType(KeyType.HASH).build())
                .build());

        waitForTable(properties.fundsTable());
    }

    private void createSubscriptionsTableIfNeeded() throws InterruptedException {
        if (tableExists(properties.subscriptionsTable())) {
            return;
        }

        dynamoDbClient.createTable(CreateTableRequest.builder()
                .tableName(properties.subscriptionsTable())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .attributeDefinitions(
                        AttributeDefinition.builder().attributeName("clientId").attributeType(ScalarAttributeType.S).build(),
                        AttributeDefinition.builder().attributeName("subscriptionId").attributeType(ScalarAttributeType.S).build(),
                        AttributeDefinition.builder().attributeName("statusOpenedAt").attributeType(ScalarAttributeType.S).build(),
                        AttributeDefinition.builder().attributeName("fundStatus").attributeType(ScalarAttributeType.S).build()
                )
                .keySchema(
                        KeySchemaElement.builder().attributeName("clientId").keyType(KeyType.HASH).build(),
                        KeySchemaElement.builder().attributeName("subscriptionId").keyType(KeyType.RANGE).build()
                )
                .globalSecondaryIndexes(
                        GlobalSecondaryIndex.builder()
                                .indexName(SUBSCRIPTION_ID_INDEX)
                                .keySchema(KeySchemaElement.builder().attributeName("subscriptionId").keyType(KeyType.HASH).build())
                                .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                                .build(),
                        GlobalSecondaryIndex.builder()
                                .indexName(CLIENT_STATUS_INDEX)
                                .keySchema(
                                        KeySchemaElement.builder().attributeName("clientId").keyType(KeyType.HASH).build(),
                                        KeySchemaElement.builder().attributeName("statusOpenedAt").keyType(KeyType.RANGE).build()
                                )
                                .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                                .build(),
                        GlobalSecondaryIndex.builder()
                                .indexName(CLIENT_FUND_STATUS_INDEX)
                                .keySchema(
                                        KeySchemaElement.builder().attributeName("clientId").keyType(KeyType.HASH).build(),
                                        KeySchemaElement.builder().attributeName("fundStatus").keyType(KeyType.RANGE).build()
                                )
                                .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                                .build()
                )
                .build());

        waitForTable(properties.subscriptionsTable());
    }

    private void createTransactionsTableIfNeeded() throws InterruptedException {
        if (tableExists(properties.transactionsTable())) {
            return;
        }

        dynamoDbClient.createTable(CreateTableRequest.builder()
                .tableName(properties.transactionsTable())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .attributeDefinitions(
                        AttributeDefinition.builder().attributeName("clientId").attributeType(ScalarAttributeType.S).build(),
                        AttributeDefinition.builder().attributeName("createdAtTransactionId").attributeType(ScalarAttributeType.S).build(),
                        AttributeDefinition.builder().attributeName("typeCreatedAt").attributeType(ScalarAttributeType.S).build()
                )
                .keySchema(
                        KeySchemaElement.builder().attributeName("clientId").keyType(KeyType.HASH).build(),
                        KeySchemaElement.builder().attributeName("createdAtTransactionId").keyType(KeyType.RANGE).build()
                )
                .globalSecondaryIndexes(
                        GlobalSecondaryIndex.builder()
                                .indexName(CLIENT_TYPE_CREATED_AT_INDEX)
                                .keySchema(
                                        KeySchemaElement.builder().attributeName("clientId").keyType(KeyType.HASH).build(),
                                        KeySchemaElement.builder().attributeName("typeCreatedAt").keyType(KeyType.RANGE).build()
                                )
                                .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                                .build()
                )
                .build());

        waitForTable(properties.transactionsTable());
    }

    private boolean tableExists(String tableName) {
        try {
            dynamoDbClient.describeTable(DescribeTableRequest.builder().tableName(tableName).build());
            return true;
        } catch (software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException exception) {
            return false;
        }
    }

    private void waitForTable(String tableName) throws InterruptedException {
        int retries = 30;
        while (retries-- > 0) {
            try {
                TableStatus tableStatus = dynamoDbClient.describeTable(
                        DescribeTableRequest.builder().tableName(tableName).build()
                ).table().tableStatus();

                if (TableStatus.ACTIVE.equals(tableStatus)) {
                    return;
                }
            } catch (software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException exception) {
                // Table creation is eventually consistent, keep polling.
            }
            Thread.sleep(Duration.ofSeconds(1));
        }

        throw new IllegalStateException("Table was not activated in time: " + tableName);
    }
}
