package com.example.funds.infrastructure.exception;

public class MissingClientContextException extends RuntimeException {

    public MissingClientContextException() {
        super("Client context is required");
    }
}
