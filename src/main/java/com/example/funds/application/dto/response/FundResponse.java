package com.example.funds.application.dto.response;

import com.example.funds.domain.model.FundCategory;

public record FundResponse(
        String fundId,
        String name,
        FundCategory category,
        long minimumAmount,
        boolean active
) {
}
