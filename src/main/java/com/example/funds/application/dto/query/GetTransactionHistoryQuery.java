package com.example.funds.application.dto.query;

import com.example.funds.domain.model.TransactionType;

import jakarta.validation.constraints.NotBlank;

public record GetTransactionHistoryQuery(
        @NotBlank String clientId,
        TransactionType type
) {
}
