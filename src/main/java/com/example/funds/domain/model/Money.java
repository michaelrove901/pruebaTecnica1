package com.example.funds.domain.model;

import java.math.BigDecimal;

public record Money(BigDecimal value) {

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    public Money {
        if (value == null) {
            throw new IllegalArgumentException("Money value cannot be null");
        }
        if (value.scale() > 0) {
            throw new IllegalArgumentException("Money value must not contain decimals");
        }
        if (value.signum() < 0) {
            throw new IllegalArgumentException("Money value cannot be negative");
        }
    }

    public static Money of(long value) {
        return new Money(BigDecimal.valueOf(value));
    }

    public Money add(Money other) {
        return new Money(value.add(other.value));
    }

    public Money subtract(Money other) {
        BigDecimal result = value.subtract(other.value);
        if (result.signum() < 0) {
            throw new IllegalArgumentException("Money value cannot be negative");
        }
        return new Money(result);
    }

    public boolean isLessThan(Money other) {
        return value.compareTo(other.value) < 0;
    }

    public long toLong() {
        return value.longValueExact();
    }
}
