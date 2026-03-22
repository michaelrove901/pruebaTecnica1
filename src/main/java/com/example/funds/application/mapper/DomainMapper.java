package com.example.funds.application.mapper;

import java.util.Comparator;
import java.util.List;

import com.example.funds.application.dto.response.ClientPortfolioResponse;
import com.example.funds.application.dto.response.FundResponse;
import com.example.funds.application.dto.response.SubscriptionItemResponse;
import com.example.funds.application.dto.response.SubscriptionResponse;
import com.example.funds.application.dto.response.TransactionResponse;
import com.example.funds.domain.model.Client;
import com.example.funds.domain.model.Fund;
import com.example.funds.domain.model.NotificationPreference;
import com.example.funds.domain.model.Subscription;
import com.example.funds.domain.model.Transaction;

public final class DomainMapper {

    private DomainMapper() {
    }

    public static FundResponse toResponse(Fund fund) {
        return new FundResponse(
                fund.fundId(),
                fund.name(),
                fund.category(),
                fund.minimumAmount().toLong(),
                fund.active()
        );
    }

    public static TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.transactionId(),
                transaction.clientId(),
                transaction.fundId(),
                transaction.subscriptionId(),
                transaction.type(),
                transaction.amount().toLong(),
                transaction.message(),
                transaction.notificationPreference(),
                transaction.createdAt()
        );
    }

    public static SubscriptionResponse toResponse(
            Subscription subscription,
            Transaction transaction,
            Client client,
            NotificationPreference notificationPreference
    ) {
        return new SubscriptionResponse(
                subscription.subscriptionId(),
                subscription.clientId(),
                subscription.fundId(),
                subscription.amount().toLong(),
                subscription.status(),
                notificationPreference,
                transaction.transactionId(),
                client.balance().toLong(),
                subscription.openedAt(),
                subscription.cancelledAt()
        );
    }

    public static SubscriptionItemResponse toItemResponse(Subscription subscription) {
        return new SubscriptionItemResponse(
                subscription.subscriptionId(),
                subscription.fundId(),
                subscription.amount().toLong(),
                subscription.status(),
                subscription.openedAt()
        );
    }

    public static ClientPortfolioResponse toPortfolioResponse(Client client, List<Subscription> subscriptions) {
        return new ClientPortfolioResponse(
                client.clientId(),
                client.fullName(),
                client.email(),
                client.balance().toLong(),
                subscriptions.stream()
                        .sorted(Comparator.comparing(Subscription::openedAt, Comparator.reverseOrder()))
                        .map(DomainMapper::toItemResponse)
                        .toList()
        );
    }
}
