package com.example.funds.domain.port.in;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.example.funds.application.dto.query.GetTransactionHistoryQuery;
import com.example.funds.application.dto.response.TransactionResponse;

public interface GetTransactionHistoryUseCase {

    List<TransactionResponse> execute(@Valid @NotNull GetTransactionHistoryQuery query);
}
