package com.example.funds.application.dto.command;

import jakarta.validation.constraints.NotBlank;

public record CancelFundSubscriptionCommand(
        @NotBlank String clientId,
        @NotBlank String subscriptionId
) {
}
