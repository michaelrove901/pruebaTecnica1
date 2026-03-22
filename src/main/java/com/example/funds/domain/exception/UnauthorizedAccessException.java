package com.example.funds.domain.exception;

public class UnauthorizedAccessException extends BusinessException {

    public UnauthorizedAccessException() {
        super(
                "UNAUTHORIZED_ACCESS",
                "You are not authorized to access this resource"
        );
    }
}
