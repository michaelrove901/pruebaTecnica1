package com.example.funds.domain.port.out;

import java.util.Optional;

import com.example.funds.domain.model.Client;

public interface ClientRepositoryPort {

    Optional<Client> findById(String clientId);

    Optional<Client> findByEmail(String email);

    Client save(Client client);
}
