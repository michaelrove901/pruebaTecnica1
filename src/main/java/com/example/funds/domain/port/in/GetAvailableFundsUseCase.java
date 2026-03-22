package com.example.funds.domain.port.in;

import java.util.List;

import com.example.funds.application.dto.response.FundResponse;

public interface GetAvailableFundsUseCase {

    List<FundResponse> execute();
}
