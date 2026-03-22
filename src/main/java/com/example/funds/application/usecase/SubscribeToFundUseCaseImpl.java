package com.example.funds.application.usecase;

import java.util.UUID;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.example.funds.application.dto.command.SubscribeToFundCommand;
import com.example.funds.application.dto.response.SubscriptionResponse;
import com.example.funds.application.mapper.DomainMapper;
import com.example.funds.domain.exception.ActiveSubscriptionAlreadyExistsException;
import com.example.funds.domain.exception.ClientNotFoundException;
import com.example.funds.domain.exception.FundNotFoundException;
import com.example.funds.domain.model.Client;
import com.example.funds.domain.model.Fund;
import com.example.funds.domain.model.Subscription;
import com.example.funds.domain.model.Transaction;
import com.example.funds.domain.port.in.SubscribeToFundUseCase;
import com.example.funds.domain.port.out.ClientRepositoryPort;
import com.example.funds.domain.port.out.FundRepositoryPort;
import com.example.funds.domain.port.out.NotificationPort;
import com.example.funds.domain.port.out.SubscriptionRepositoryPort;
import com.example.funds.domain.port.out.TransactionRepositoryPort;

@Service
@Validated
public class SubscribeToFundUseCaseImpl implements SubscribeToFundUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscribeToFundUseCaseImpl.class);

    private final ClientRepositoryPort clientRepositoryPort;
    private final FundRepositoryPort fundRepositoryPort;
    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final TransactionRepositoryPort transactionRepositoryPort;
    private final NotificationPort notificationPort;

    public SubscribeToFundUseCaseImpl(
            ClientRepositoryPort clientRepositoryPort,
            FundRepositoryPort fundRepositoryPort,
            SubscriptionRepositoryPort subscriptionRepositoryPort,
            TransactionRepositoryPort transactionRepositoryPort,
            NotificationPort notificationPort
    ) {
        this.clientRepositoryPort = clientRepositoryPort;
        this.fundRepositoryPort = fundRepositoryPort;
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
        this.transactionRepositoryPort = transactionRepositoryPort;
        this.notificationPort = notificationPort;
    }

    @Override
    public SubscriptionResponse execute(@Valid SubscribeToFundCommand command) {
        LOGGER.info("Processing fund subscription. clientId={}, fundId={}", command.clientId(), command.fundId());

        Client client = clientRepositoryPort.findById(command.clientId())
                .orElseThrow(() -> new ClientNotFoundException(command.clientId()));

        Fund fund = fundRepositoryPort.findById(command.fundId())
                .orElseThrow(() -> new FundNotFoundException(command.fundId()));

        fund.validateAvailability();

        subscriptionRepositoryPort.findActiveByClientIdAndFundId(client.clientId(), fund.fundId())
                .ifPresent(subscription -> {
                    throw new ActiveSubscriptionAlreadyExistsException(client.clientId(), fund.fundId());
                });

        client.debitForFund(fund);

        Subscription subscription = Subscription.create(
                UUID.randomUUID().toString(),
                client.clientId(),
                fund.fundId(),
                fund.minimumAmount()
        );

        Transaction transaction = Transaction.subscription(
                client.clientId(),
                fund.fundId(),
                subscription.subscriptionId(),
                fund.minimumAmount(),
                command.notificationPreference()
        );

        clientRepositoryPort.save(client);
        subscriptionRepositoryPort.save(subscription);
        transactionRepositoryPort.save(transaction);
        notificationPort.sendSubscriptionNotification(client, fund, transaction);

        LOGGER.info(
                "Fund subscription completed. clientId={}, fundId={}, subscriptionId={}, transactionId={}",
                client.clientId(),
                fund.fundId(),
                subscription.subscriptionId(),
                transaction.transactionId()
        );

        return DomainMapper.toResponse(subscription, transaction, client, command.notificationPreference());
    }
}
