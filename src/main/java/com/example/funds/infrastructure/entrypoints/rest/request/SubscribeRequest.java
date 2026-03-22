package com.example.funds.infrastructure.entrypoints.rest.request;

import com.example.funds.domain.model.NotificationPreference;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubscribeRequest(
        @NotBlank String fundId,
        @NotNull NotificationPreference notificationPreference
) {
}
