package com.example.funds.domain.exception;

public class ClientNotFoundException extends ResourceNotFoundException {

    public ClientNotFoundException(String clientId) {
        super("CLIENT_NOT_FOUND", "Client not found: " + clientId);
    }
}
