package com.example.funds.domain.exception;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String code, String message) {
        super(code, message);
    }
}
