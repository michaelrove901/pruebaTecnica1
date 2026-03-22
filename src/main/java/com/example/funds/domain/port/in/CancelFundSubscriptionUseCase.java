package com.example.funds.domain.port.in;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.example.funds.application.dto.command.CancelFundSubscriptionCommand;
import com.example.funds.application.dto.response.SubscriptionResponse;

public interface CancelFundSubscriptionUseCase {

    SubscriptionResponse execute(@Valid @NotNull CancelFundSubscriptionCommand command);
}
