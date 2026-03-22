package com.example.funds.domain.port.in;

import com.example.funds.application.dto.command.CancelFundSubscriptionCommand;
import com.example.funds.application.dto.response.SubscriptionResponse;

public interface CancelFundSubscriptionUseCase {

    SubscriptionResponse execute(CancelFundSubscriptionCommand command);
}
