package com.example.funds.infrastructure.adapters.persistence.dynamodb.mapper;

import java.util.Map;

import com.example.funds.domain.model.Fund;
import com.example.funds.domain.model.FundCategory;
import com.example.funds.domain.model.Money;
import com.example.funds.infrastructure.adapters.persistence.dynamodb.document.FundItem;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public final class FundDynamoDbMapper {

    private FundDynamoDbMapper() {
    }

    public static FundItem toItem(Fund fund) {
        return new FundItem(
                fund.fundId(),
                fund.name(),
                fund.category().name(),
                fund.minimumAmount().toLong(),
                fund.active()
        );
    }

    public static Map<String, AttributeValue> toAttributes(FundItem item) {
        return Map.of(
                "fundId", AttributeValue.fromS(item.fundId()),
                "name", AttributeValue.fromS(item.name()),
                "category", AttributeValue.fromS(item.category()),
                "minimumAmount", AttributeValue.fromN(String.valueOf(item.minimumAmount())),
                "active", AttributeValue.fromBool(item.active())
        );
    }

    public static Fund toDomain(Map<String, AttributeValue> attributes) {
        return new Fund(
                attributes.get("fundId").s(),
                attributes.get("name").s(),
                FundCategory.valueOf(attributes.get("category").s()),
                Money.of(Long.parseLong(attributes.get("minimumAmount").n())),
                attributes.get("active").bool()
        );
    }
}
