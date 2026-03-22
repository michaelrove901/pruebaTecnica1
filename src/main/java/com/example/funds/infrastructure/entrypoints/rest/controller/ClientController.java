package com.example.funds.infrastructure.entrypoints.rest.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.funds.application.dto.query.GetClientPortfolioQuery;
import com.example.funds.domain.port.in.GetClientPortfolioUseCase;
import com.example.funds.infrastructure.entrypoints.rest.mapper.RestResponseMapper;
import com.example.funds.infrastructure.entrypoints.rest.response.ClientPortfolioRestResponse;
import com.example.funds.infrastructure.entrypoints.rest.support.AuthenticatedClientProvider;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@Validated
@RequestMapping("/api/v1/clients")
public class ClientController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientController.class);

    private final GetClientPortfolioUseCase getClientPortfolioUseCase;
    private final AuthenticatedClientProvider authenticatedClientProvider;

    public ClientController(
            GetClientPortfolioUseCase getClientPortfolioUseCase,
            AuthenticatedClientProvider authenticatedClientProvider
    ) {
        this.getClientPortfolioUseCase = getClientPortfolioUseCase;
        this.authenticatedClientProvider = authenticatedClientProvider;
    }

    @GetMapping("/me/portfolio")
    public ResponseEntity<ClientPortfolioRestResponse> getPortfolio(HttpServletRequest request) {
        String clientId = authenticatedClientProvider.getCurrentClientId(request);
        LOGGER.info("Received request to get client portfolio. clientId={}", clientId);

        return ResponseEntity.ok(
                RestResponseMapper.toRest(getClientPortfolioUseCase.execute(new GetClientPortfolioQuery(clientId)))
        );
    }
}
