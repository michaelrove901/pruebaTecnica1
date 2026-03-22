package com.example.funds.application.dto.response;

import java.util.List;

public record ClientPortfolioResponse(
        String clientId,
        String fullName,
        String email,
        long balance,
        List<SubscriptionItemResponse> activeSubscriptions
) {
}
