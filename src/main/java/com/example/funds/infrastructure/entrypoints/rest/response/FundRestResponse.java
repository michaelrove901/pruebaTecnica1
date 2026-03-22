package com.example.funds.infrastructure.entrypoints.rest.response;

import com.example.funds.domain.model.FundCategory;

public record FundRestResponse(
        String fundId,
        String name,
        FundCategory category,
        long minimumAmount,
        boolean active
) {
}
