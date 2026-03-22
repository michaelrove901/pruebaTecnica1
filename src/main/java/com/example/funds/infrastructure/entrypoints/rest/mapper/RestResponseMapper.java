package com.example.funds.infrastructure.entrypoints.rest.mapper;

import java.util.List;

import com.example.funds.application.dto.response.ClientPortfolioResponse;
import com.example.funds.application.dto.response.FundResponse;
import com.example.funds.application.dto.response.SubscriptionItemResponse;
import com.example.funds.application.dto.response.SubscriptionResponse;
import com.example.funds.application.dto.response.TransactionResponse;
import com.example.funds.infrastructure.entrypoints.rest.response.ClientPortfolioRestResponse;
import com.example.funds.infrastructure.entrypoints.rest.response.FundRestResponse;
import com.example.funds.infrastructure.entrypoints.rest.response.SubscriptionItemRestResponse;
import com.example.funds.infrastructure.entrypoints.rest.response.SubscriptionRestResponse;
import com.example.funds.infrastructure.entrypoints.rest.response.TransactionRestResponse;

public final class RestResponseMapper {

    private RestResponseMapper() {
    }

    public static FundRestResponse toRest(FundResponse response) {
        return new FundRestResponse(
                response.fundId(),
                response.name(),
                response.category(),
                response.minimumAmount(),
                response.active()
        );
    }

    public static List<FundRestResponse> toFundRestList(List<FundResponse> responses) {
        return responses.stream().map(RestResponseMapper::toRest).toList();
    }

    public static SubscriptionRestResponse toRest(SubscriptionResponse response) {
        return new SubscriptionRestResponse(
                response.subscriptionId(),
                response.clientId(),
                response.fundId(),
                response.amount(),
                response.status(),
                response.notificationPreference(),
                response.transactionId(),
                response.currentBalance(),
                response.openedAt(),
                response.cancelledAt()
        );
    }

    public static TransactionRestResponse toRest(TransactionResponse response) {
        return new TransactionRestResponse(
                response.transactionId(),
                response.clientId(),
                response.fundId(),
                response.subscriptionId(),
                response.type(),
                response.amount(),
                response.message(),
                response.notificationPreference(),
                response.createdAt()
        );
    }

    public static List<TransactionRestResponse> toTransactionRestList(List<TransactionResponse> responses) {
        return responses.stream().map(RestResponseMapper::toRest).toList();
    }

    public static ClientPortfolioRestResponse toRest(ClientPortfolioResponse response) {
        return new ClientPortfolioRestResponse(
                response.clientId(),
                response.fullName(),
                response.email(),
                response.balance(),
                response.activeSubscriptions().stream().map(RestResponseMapper::toRest).toList()
        );
    }

    private static SubscriptionItemRestResponse toRest(SubscriptionItemResponse response) {
        return new SubscriptionItemRestResponse(
                response.subscriptionId(),
                response.fundId(),
                response.amount(),
                response.status(),
                response.openedAt()
        );
    }
}
