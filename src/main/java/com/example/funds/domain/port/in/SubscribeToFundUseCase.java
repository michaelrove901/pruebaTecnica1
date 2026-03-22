package com.example.funds.domain.port.in;

import com.example.funds.application.dto.command.SubscribeToFundCommand;
import com.example.funds.application.dto.response.SubscriptionResponse;

public interface SubscribeToFundUseCase {

    SubscriptionResponse execute(SubscribeToFundCommand command);
}
