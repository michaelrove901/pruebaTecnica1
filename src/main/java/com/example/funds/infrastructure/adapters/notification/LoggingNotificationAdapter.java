package com.example.funds.infrastructure.adapters.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.funds.domain.model.Client;
import com.example.funds.domain.model.Fund;
import com.example.funds.domain.model.Transaction;
import com.example.funds.domain.port.out.NotificationPort;

@Component
public class LoggingNotificationAdapter implements NotificationPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingNotificationAdapter.class);

    @Override
    public void sendSubscriptionNotification(Client client, Fund fund, Transaction transaction) {
        LOGGER.info(
                "Subscription notification sent. channel={}, clientId={}, fundId={}, transactionId={}",
                transaction.notificationPreference(),
                client.clientId(),
                fund.fundId(),
                transaction.transactionId()
        );
    }
}
