package com.example.funds.domain.exception;

public class FundInactiveException extends BusinessException {

    public FundInactiveException(String fundId) {
        super(
                "FUND_INACTIVE",
                "Fund is not active: " + fundId
        );
    }
}
