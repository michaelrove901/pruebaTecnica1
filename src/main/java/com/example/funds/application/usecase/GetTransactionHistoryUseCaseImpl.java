package com.example.funds.application.usecase;

import java.util.Comparator;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.example.funds.application.dto.query.GetTransactionHistoryQuery;
import com.example.funds.application.dto.response.TransactionResponse;
import com.example.funds.application.mapper.DomainMapper;
import com.example.funds.domain.exception.ClientNotFoundException;
import com.example.funds.domain.port.in.GetTransactionHistoryUseCase;
import com.example.funds.domain.port.out.ClientRepositoryPort;
import com.example.funds.domain.port.out.TransactionRepositoryPort;

@Service
@Validated
public class GetTransactionHistoryUseCaseImpl implements GetTransactionHistoryUseCase {

    private final ClientRepositoryPort clientRepositoryPort;
    private final TransactionRepositoryPort transactionRepositoryPort;

    public GetTransactionHistoryUseCaseImpl(
            ClientRepositoryPort clientRepositoryPort,
            TransactionRepositoryPort transactionRepositoryPort
    ) {
        this.clientRepositoryPort = clientRepositoryPort;
        this.transactionRepositoryPort = transactionRepositoryPort;
    }

    @Override
    public List<TransactionResponse> execute(@Valid GetTransactionHistoryQuery query) {
        clientRepositoryPort.findById(query.clientId())
                .orElseThrow(() -> new ClientNotFoundException(query.clientId()));

        if (query.type() == null) {
            return transactionRepositoryPort.findByClientId(query.clientId())
                    .stream()
                    .sorted(Comparator.comparing(transaction -> transaction.createdAt(), Comparator.reverseOrder()))
                    .map(DomainMapper::toResponse)
                    .toList();
        }

        return transactionRepositoryPort.findByClientIdAndType(query.clientId(), query.type())
                .stream()
                .sorted(Comparator.comparing(transaction -> transaction.createdAt(), Comparator.reverseOrder()))
                .map(DomainMapper::toResponse)
                .toList();
    }
}
