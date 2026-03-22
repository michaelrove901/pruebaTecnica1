package com.example.funds.domain.model;

import java.util.Objects;

import com.example.funds.domain.exception.FundInactiveException;

public record Fund(
        String fundId,
        String name,
        FundCategory category,
        Money minimumAmount,
        boolean active
) {

    public Fund {
        Objects.requireNonNull(fundId, "fundId is required");
        Objects.requireNonNull(name, "name is required");
        Objects.requireNonNull(category, "category is required");
        Objects.requireNonNull(minimumAmount, "minimumAmount is required");
        if (minimumAmount.toLong() <= 0) {
            throw new IllegalArgumentException("minimumAmount must be greater than zero");
        }
    }

    public void validateAvailability() {
        if (!active) {
            throw new FundInactiveException(fundId);
        }
    }
}
