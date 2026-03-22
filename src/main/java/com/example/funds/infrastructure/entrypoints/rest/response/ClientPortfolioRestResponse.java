package com.example.funds.infrastructure.entrypoints.rest.response;

import java.util.List;

public record ClientPortfolioRestResponse(
        String clientId,
        String fullName,
        String email,
        long balance,
        List<SubscriptionItemRestResponse> activeSubscriptions
) {
}
