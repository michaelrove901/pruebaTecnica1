package com.example.funds.infrastructure.entrypoints.rest.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.funds.infrastructure.entrypoints.rest.request.LoginRequest;
import com.example.funds.infrastructure.entrypoints.rest.response.LoginResponse;
import com.example.funds.infrastructure.security.AuthenticatedClientUser;
import com.example.funds.infrastructure.security.JwtProperties;
import com.example.funds.infrastructure.security.JwtTokenService;

import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtTokenService jwtTokenService,
            JwtProperties jwtProperties
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.jwtProperties = jwtProperties;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LOGGER.info("Received login request for email={}", request.email());

        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        AuthenticatedClientUser principal = (AuthenticatedClientUser) authentication.getPrincipal();
        String token = jwtTokenService.generateToken(principal);

        return ResponseEntity.ok(new LoginResponse(token, "Bearer", jwtProperties.expirationMinutes()));
    }
}
