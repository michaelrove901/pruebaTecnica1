package com.example.funds.infrastructure.entrypoints.rest.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.funds.application.dto.query.GetTransactionHistoryQuery;
import com.example.funds.domain.model.TransactionType;
import com.example.funds.domain.port.in.GetTransactionHistoryUseCase;
import com.example.funds.infrastructure.entrypoints.rest.mapper.RestResponseMapper;
import com.example.funds.infrastructure.entrypoints.rest.response.TransactionRestResponse;
import com.example.funds.infrastructure.entrypoints.rest.support.AuthenticatedClientProvider;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@Validated
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionController.class);

    private final GetTransactionHistoryUseCase getTransactionHistoryUseCase;
    private final AuthenticatedClientProvider authenticatedClientProvider;

    public TransactionController(
            GetTransactionHistoryUseCase getTransactionHistoryUseCase,
            AuthenticatedClientProvider authenticatedClientProvider
    ) {
        this.getTransactionHistoryUseCase = getTransactionHistoryUseCase;
        this.authenticatedClientProvider = authenticatedClientProvider;
    }

    @GetMapping
    public ResponseEntity<List<TransactionRestResponse>> getTransactions(
            @RequestParam(required = false) TransactionType type,
            HttpServletRequest request
    ) {
        String clientId = authenticatedClientProvider.getCurrentClientId(request);
        LOGGER.info("Received request to get transactions. clientId={}, type={}", clientId, type);

        return ResponseEntity.ok(
                RestResponseMapper.toTransactionRestList(
                        getTransactionHistoryUseCase.execute(new GetTransactionHistoryQuery(clientId, type))
                )
        );
    }
}
