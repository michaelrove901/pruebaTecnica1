package com.example.funds.domain.model;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.example.funds.domain.exception.InsufficientBalanceException;

public class Client {

    public static final Money INITIAL_BALANCE = Money.of(500000);

    private final String clientId;
    private final String fullName;
    private final String email;
    private final String phone;
    private final String passwordHash;
    private final Set<Role> roles;
    private final OffsetDateTime createdAt;
    private Money balance;

    public Client(
            String clientId,
            String fullName,
            String email,
            String phone,
            String passwordHash,
            Set<Role> roles,
            Money balance,
            OffsetDateTime createdAt
    ) {
        this.clientId = Objects.requireNonNull(clientId, "clientId is required");
        this.fullName = Objects.requireNonNull(fullName, "fullName is required");
        this.email = Objects.requireNonNull(email, "email is required");
        this.phone = Objects.requireNonNull(phone, "phone is required");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash is required");
        this.roles = new HashSet<>(Objects.requireNonNullElse(roles, Set.of(Role.CLIENT)));
        this.balance = Objects.requireNonNullElse(balance, INITIAL_BALANCE);
        this.createdAt = Objects.requireNonNullElseGet(createdAt, OffsetDateTime::now);
    }

    public static Client create(
            String clientId,
            String fullName,
            String email,
            String phone,
            String passwordHash,
            Set<Role> roles
    ) {
        return new Client(clientId, fullName, email, phone, passwordHash, roles, INITIAL_BALANCE, OffsetDateTime.now());
    }

    public void debitForFund(Fund fund) {
        Objects.requireNonNull(fund, "fund is required");
        if (balance.isLessThan(fund.minimumAmount())) {
            throw new InsufficientBalanceException(fund.name());
        }
        balance = balance.subtract(fund.minimumAmount());
    }

    public void credit(Money amount) {
        Objects.requireNonNull(amount, "amount is required");
        balance = balance.add(amount);
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    public String clientId() {
        return clientId;
    }

    public String fullName() {
        return fullName;
    }

    public String email() {
        return email;
    }

    public String phone() {
        return phone;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public Set<Role> roles() {
        return Collections.unmodifiableSet(roles);
    }

    public Money balance() {
        return balance;
    }

    public OffsetDateTime createdAt() {
        return createdAt;
    }
}
