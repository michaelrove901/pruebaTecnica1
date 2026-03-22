package com.example.funds.application.usecase;

import jakarta.validation.Valid;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.example.funds.application.dto.query.GetClientPortfolioQuery;
import com.example.funds.application.dto.response.ClientPortfolioResponse;
import com.example.funds.application.mapper.DomainMapper;
import com.example.funds.domain.exception.ClientNotFoundException;
import com.example.funds.domain.model.SubscriptionStatus;
import com.example.funds.domain.port.in.GetClientPortfolioUseCase;
import com.example.funds.domain.port.out.ClientRepositoryPort;
import com.example.funds.domain.port.out.SubscriptionRepositoryPort;

@Service
@Validated
public class GetClientPortfolioUseCaseImpl implements GetClientPortfolioUseCase {

    private final ClientRepositoryPort clientRepositoryPort;
    private final SubscriptionRepositoryPort subscriptionRepositoryPort;

    public GetClientPortfolioUseCaseImpl(
            ClientRepositoryPort clientRepositoryPort,
            SubscriptionRepositoryPort subscriptionRepositoryPort
    ) {
        this.clientRepositoryPort = clientRepositoryPort;
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
    }

    @Override
    public ClientPortfolioResponse execute(@Valid GetClientPortfolioQuery query) {
        var client = clientRepositoryPort.findById(query.clientId())
                .orElseThrow(() -> new ClientNotFoundException(query.clientId()));

        var subscriptions = subscriptionRepositoryPort.findByClientIdAndStatus(
                query.clientId(),
                SubscriptionStatus.ACTIVE
        );

        return DomainMapper.toPortfolioResponse(client, subscriptions);
    }
}
