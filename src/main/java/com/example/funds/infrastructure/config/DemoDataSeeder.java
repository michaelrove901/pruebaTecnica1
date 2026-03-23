package com.example.funds.infrastructure.config;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.funds.domain.model.Client;
import com.example.funds.domain.model.Fund;
import com.example.funds.domain.model.FundCategory;
import com.example.funds.domain.model.Money;
import com.example.funds.domain.model.Role;
import com.example.funds.infrastructure.adapters.persistence.dynamodb.mapper.ClientDynamoDbMapper;
import com.example.funds.infrastructure.adapters.persistence.dynamodb.mapper.FundDynamoDbMapper;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

@Component
public class DemoDataSeeder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoDataSeeder.class);

    public static final String DEMO_CLIENT_ID = "client-local-001";
    public static final String DEMO_CLIENT_EMAIL = "client.local@example.com";
    public static final String DEMO_CLIENT_PASSWORD = "Password123!";
    private static final String DEMO_CLIENT_PHONE = "+573001112233";
    private static final String DEMO_CLIENT_NAME = "Cliente Local";

    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbProperties properties;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(
            DynamoDbClient dynamoDbClient,
            DynamoDbProperties properties,
            PasswordEncoder passwordEncoder
    ) {
        this.dynamoDbClient = dynamoDbClient;
        this.properties = properties;
        this.passwordEncoder = passwordEncoder;
    }

    public void seedDemoDataIfMissing() {
        seedFundsIfMissing();
        seedDemoClientIfMissing();
    }

    public void logDemoCredentials() {
        LOGGER.info("Demo credentials available. email={}, password={}", DEMO_CLIENT_EMAIL, DEMO_CLIENT_PASSWORD);
    }

    private void seedFundsIfMissing() {
        for (Fund fund : initialFunds()) {
            if (itemExists(properties.fundsTable(), MapKeys.fundId(fund.fundId()))) {
                continue;
            }

            dynamoDbClient.putItem(PutItemRequest.builder()
                    .tableName(properties.fundsTable())
                    .item(FundDynamoDbMapper.toAttributes(FundDynamoDbMapper.toItem(fund)))
                    .build());
            LOGGER.info("Demo fund created. fundId={}", fund.fundId());
        }
    }

    private void seedDemoClientIfMissing() {
        if (itemExists(properties.clientsTable(), MapKeys.clientId(DEMO_CLIENT_ID))) {
            return;
        }

        Client demoClient = Client.create(
                DEMO_CLIENT_ID,
                DEMO_CLIENT_NAME,
                DEMO_CLIENT_EMAIL,
                DEMO_CLIENT_PHONE,
                passwordEncoder.encode(DEMO_CLIENT_PASSWORD),
                Set.of(Role.CLIENT)
        );

        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(properties.clientsTable())
                .item(ClientDynamoDbMapper.toAttributes(ClientDynamoDbMapper.toItem(demoClient)))
                .build());

        LOGGER.info("Demo client created. clientId={}, email={}", DEMO_CLIENT_ID, DEMO_CLIENT_EMAIL);
    }

    private boolean itemExists(String tableName, java.util.Map<String, AttributeValue> key) {
        var response = dynamoDbClient.getItem(GetItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build());

        return response.hasItem() && !response.item().isEmpty();
    }

    private List<Fund> initialFunds() {
        return List.of(
                new Fund("FPV_BTG_PACTUAL_RECAUDADORA", "FPV_BTG_PACTUAL_RECAUDADORA", FundCategory.FPV, Money.of(75000), true),
                new Fund("FPV_BTG_PACTUAL_ECOPETROL", "FPV_BTG_PACTUAL_ECOPETROL", FundCategory.FPV, Money.of(125000), true),
                new Fund("DEUDAPRIVADA", "DEUDAPRIVADA", FundCategory.FIC, Money.of(50000), true),
                new Fund("FDO-ACCIONES", "FDO-ACCIONES", FundCategory.FIC, Money.of(250000), true),
                new Fund("FPV_BTG_PACTUAL_DINAMICA", "FPV_BTG_PACTUAL_DINAMICA", FundCategory.FPV, Money.of(100000), true)
        );
    }

    private static final class MapKeys {

        private MapKeys() {
        }

        private static java.util.Map<String, AttributeValue> fundId(String fundId) {
            return java.util.Map.of("fundId", AttributeValue.fromS(fundId));
        }

        private static java.util.Map<String, AttributeValue> clientId(String clientId) {
            return java.util.Map.of("clientId", AttributeValue.fromS(clientId));
        }
    }
}
