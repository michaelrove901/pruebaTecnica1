package com.example.funds.application.dto.command;

import com.example.funds.domain.model.NotificationPreference;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubscribeToFundCommand(
        @NotBlank String clientId,
        @NotBlank String fundId,
        @NotNull NotificationPreference notificationPreference
) {
}
