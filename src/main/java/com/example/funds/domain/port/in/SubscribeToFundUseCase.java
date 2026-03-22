package com.example.funds.domain.port.in;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.example.funds.application.dto.command.SubscribeToFundCommand;
import com.example.funds.application.dto.response.SubscriptionResponse;

public interface SubscribeToFundUseCase {

    SubscriptionResponse execute(@Valid @NotNull SubscribeToFundCommand command);
}
