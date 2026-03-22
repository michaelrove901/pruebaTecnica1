package com.example.funds.domain.port.in;

import com.example.funds.application.dto.query.GetClientPortfolioQuery;
import com.example.funds.application.dto.response.ClientPortfolioResponse;

public interface GetClientPortfolioUseCase {

    ClientPortfolioResponse execute(GetClientPortfolioQuery query);
}
