package com.example.funds.infrastructure.entrypoints.rest.controller;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.funds.application.dto.command.CancelFundSubscriptionCommand;
import com.example.funds.application.dto.command.SubscribeToFundCommand;
import com.example.funds.domain.port.in.CancelFundSubscriptionUseCase;
import com.example.funds.domain.port.in.SubscribeToFundUseCase;
import com.example.funds.infrastructure.entrypoints.rest.mapper.RestResponseMapper;
import com.example.funds.infrastructure.entrypoints.rest.request.SubscribeRequest;
import com.example.funds.infrastructure.entrypoints.rest.response.SubscriptionRestResponse;
import com.example.funds.infrastructure.entrypoints.rest.support.AuthenticatedClientProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@Validated
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionController.class);

    private final SubscribeToFundUseCase subscribeToFundUseCase;
    private final CancelFundSubscriptionUseCase cancelFundSubscriptionUseCase;
    private final AuthenticatedClientProvider authenticatedClientProvider;

    public SubscriptionController(
            SubscribeToFundUseCase subscribeToFundUseCase,
            CancelFundSubscriptionUseCase cancelFundSubscriptionUseCase,
            AuthenticatedClientProvider authenticatedClientProvider
    ) {
        this.subscribeToFundUseCase = subscribeToFundUseCase;
        this.cancelFundSubscriptionUseCase = cancelFundSubscriptionUseCase;
        this.authenticatedClientProvider = authenticatedClientProvider;
    }

    @PostMapping
    public ResponseEntity<SubscriptionRestResponse> subscribe(
            @Valid @RequestBody SubscribeRequest requestBody,
            HttpServletRequest request
    ) {
        String clientId = authenticatedClientProvider.getCurrentClientId(request);
        LOGGER.info("Received request to subscribe to fund. clientId={}, fundId={}", clientId, requestBody.fundId());

        SubscriptionRestResponse response = RestResponseMapper.toRest(
                subscribeToFundUseCase.execute(
                        new SubscribeToFundCommand(clientId, requestBody.fundId(), requestBody.notificationPreference())
                )
        );

        return ResponseEntity
                .created(URI.create("/api/v1/subscriptions/" + response.subscriptionId()))
                .body(response);
    }

    @DeleteMapping("/{subscriptionId}")
    public ResponseEntity<SubscriptionRestResponse> cancel(
            @PathVariable @NotBlank String subscriptionId,
            HttpServletRequest request
    ) {
        String clientId = authenticatedClientProvider.getCurrentClientId(request);
        LOGGER.info("Received request to cancel subscription. clientId={}, subscriptionId={}", clientId, subscriptionId);

        return ResponseEntity.ok(
                RestResponseMapper.toRest(
                        cancelFundSubscriptionUseCase.execute(
                                new CancelFundSubscriptionCommand(clientId, subscriptionId)
                        )
                )
        );
    }
}
