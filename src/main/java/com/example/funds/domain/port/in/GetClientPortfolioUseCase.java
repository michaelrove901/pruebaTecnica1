package com.example.funds.domain.port.in;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.example.funds.application.dto.query.GetClientPortfolioQuery;
import com.example.funds.application.dto.response.ClientPortfolioResponse;

public interface GetClientPortfolioUseCase {

    ClientPortfolioResponse execute(@Valid @NotNull GetClientPortfolioQuery query);
}
