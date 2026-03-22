package com.example.funds.domain.port.out;

import com.example.funds.domain.model.Client;
import com.example.funds.domain.model.Fund;
import com.example.funds.domain.model.Transaction;

public interface NotificationPort {

    void sendSubscriptionNotification(Client client, Fund fund, Transaction transaction);
}
