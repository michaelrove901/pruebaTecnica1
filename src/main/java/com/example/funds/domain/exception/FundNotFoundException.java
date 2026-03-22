package com.example.funds.domain.exception;

public class FundNotFoundException extends ResourceNotFoundException {

    public FundNotFoundException(String fundId) {
        super("FUND_NOT_FOUND", "Fund not found: " + fundId);
    }
}
