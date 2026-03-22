package com.example.funds.infrastructure.entrypoints.rest.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.funds.domain.port.in.GetAvailableFundsUseCase;
import com.example.funds.infrastructure.entrypoints.rest.mapper.RestResponseMapper;
import com.example.funds.infrastructure.entrypoints.rest.response.FundRestResponse;

@RestController
@Validated
@RequestMapping("/api/v1/funds")
public class FundController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FundController.class);

    private final GetAvailableFundsUseCase getAvailableFundsUseCase;

    public FundController(GetAvailableFundsUseCase getAvailableFundsUseCase) {
        this.getAvailableFundsUseCase = getAvailableFundsUseCase;
    }

    @GetMapping
    public ResponseEntity<List<FundRestResponse>> getFunds() {
        LOGGER.info("Received request to list available funds");
        return ResponseEntity.ok(RestResponseMapper.toFundRestList(getAvailableFundsUseCase.execute()));
    }
}
