package com.example.funds.domain.port.out;

import java.util.List;

import com.example.funds.domain.model.Transaction;
import com.example.funds.domain.model.TransactionType;

public interface TransactionRepositoryPort {

    Transaction save(Transaction transaction);

    List<Transaction> findByClientId(String clientId);

    List<Transaction> findByClientIdAndType(String clientId, TransactionType type);
}
