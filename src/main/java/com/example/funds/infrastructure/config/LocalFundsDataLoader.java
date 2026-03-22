package com.example.funds.infrastructure.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.example.funds.domain.model.Fund;
import com.example.funds.domain.model.FundCategory;
import com.example.funds.domain.model.Money;
import com.example.funds.infrastructure.adapters.persistence.dynamodb.mapper.FundDynamoDbMapper;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

@Component
@Profile("local")
public class LocalFundsDataLoader implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalFundsDataLoader.class);

    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbProperties properties;

    public LocalFundsDataLoader(DynamoDbClient dynamoDbClient, DynamoDbProperties properties) {
        this.dynamoDbClient = dynamoDbClient;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<Fund> initialFunds = List.of(
                new Fund("FPV_BTG_PACTUAL_RECAUDADORA", "FPV_BTG_PACTUAL_RECAUDADORA", FundCategory.FPV, Money.of(75000), true),
                new Fund("FPV_BTG_PACTUAL_ECOPETROL", "FPV_BTG_PACTUAL_ECOPETROL", FundCategory.FPV, Money.of(125000), true),
                new Fund("DEUDAPRIVADA", "DEUDAPRIVADA", FundCategory.FIC, Money.of(50000), true),
                new Fund("FDO-ACCIONES", "FDO-ACCIONES", FundCategory.FIC, Money.of(250000), true),
                new Fund("FPV_BTG_PACTUAL_DINAMICA", "FPV_BTG_PACTUAL_DINAMICA", FundCategory.FPV, Money.of(100000), true)
        );

        try {
            for (Fund fund : initialFunds) {
                dynamoDbClient.putItem(PutItemRequest.builder()
                        .tableName(properties.fundsTable())
                        .item(FundDynamoDbMapper.toAttributes(FundDynamoDbMapper.toItem(fund)))
                        .build());
            }

            LOGGER.info("Local fund catalog loaded. funds={}", initialFunds.size());
        } catch (Exception exception) {
            LOGGER.warn("Local fund catalog could not be loaded. table={}", properties.fundsTable());
        }
    }
}
