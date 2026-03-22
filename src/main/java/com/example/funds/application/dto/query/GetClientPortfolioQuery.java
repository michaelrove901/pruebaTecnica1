package com.example.funds.application.dto.query;

import jakarta.validation.constraints.NotBlank;

public record GetClientPortfolioQuery(@NotBlank String clientId) {
}
