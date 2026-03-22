package com.example.funds.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.example.funds.application.dto.command.CancelFundSubscriptionCommand;
import com.example.funds.application.dto.response.SubscriptionResponse;
import com.example.funds.application.mapper.DomainMapper;
import com.example.funds.domain.exception.ClientNotFoundException;
import com.example.funds.domain.exception.SubscriptionNotFoundException;
import com.example.funds.domain.exception.UnauthorizedAccessException;
import com.example.funds.domain.model.Client;
import com.example.funds.domain.model.Subscription;
import com.example.funds.domain.model.Transaction;
import com.example.funds.domain.port.in.CancelFundSubscriptionUseCase;
import com.example.funds.domain.port.out.ClientRepositoryPort;
import com.example.funds.domain.port.out.SubscriptionRepositoryPort;
import com.example.funds.domain.port.out.TransactionRepositoryPort;

@Service
@Validated
public class CancelFundSubscriptionUseCaseImpl implements CancelFundSubscriptionUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(CancelFundSubscriptionUseCaseImpl.class);

    private final ClientRepositoryPort clientRepositoryPort;
    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final TransactionRepositoryPort transactionRepositoryPort;

    public CancelFundSubscriptionUseCaseImpl(
            ClientRepositoryPort clientRepositoryPort,
            SubscriptionRepositoryPort subscriptionRepositoryPort,
            TransactionRepositoryPort transactionRepositoryPort
    ) {
        this.clientRepositoryPort = clientRepositoryPort;
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
        this.transactionRepositoryPort = transactionRepositoryPort;
    }

    @Override
    public SubscriptionResponse execute(CancelFundSubscriptionCommand command) {
        LOGGER.info("Processing subscription cancellation. clientId={}, subscriptionId={}", command.clientId(), command.subscriptionId());

        Client client = clientRepositoryPort.findById(command.clientId())
                .orElseThrow(() -> new ClientNotFoundException(command.clientId()));

        Subscription subscription = subscriptionRepositoryPort.findById(command.subscriptionId())
                .orElseThrow(() -> new SubscriptionNotFoundException(command.subscriptionId()));

        if (!subscription.clientId().equals(command.clientId())) {
            throw new UnauthorizedAccessException();
        }

        subscription.cancel();
        client.credit(subscription.amount());

        Transaction transaction = Transaction.cancellation(
                client.clientId(),
                subscription.fundId(),
                subscription.subscriptionId(),
                subscription.amount()
        );

        clientRepositoryPort.save(client);
        subscriptionRepositoryPort.save(subscription);
        transactionRepositoryPort.save(transaction);

        LOGGER.info(
                "Subscription cancellation completed. clientId={}, subscriptionId={}, transactionId={}",
                client.clientId(),
                subscription.subscriptionId(),
                transaction.transactionId()
        );

        return DomainMapper.toResponse(subscription, transaction, client, null);
    }
}
