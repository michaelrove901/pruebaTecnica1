package com.example.funds.infrastructure.entrypoints.rest.support;

import jakarta.servlet.http.HttpServletRequest;

public interface AuthenticatedClientProvider {

    String getCurrentClientId(HttpServletRequest request);
}
