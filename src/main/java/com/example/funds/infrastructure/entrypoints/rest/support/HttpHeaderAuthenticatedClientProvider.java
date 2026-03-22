package com.example.funds.infrastructure.entrypoints.rest.support;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.example.funds.infrastructure.exception.MissingClientContextException;
import com.example.funds.infrastructure.security.AuthenticatedClientUser;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class HttpHeaderAuthenticatedClientProvider implements AuthenticatedClientProvider {

    @Override
    public String getCurrentClientId(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedClientUser user) {
            return user.clientId();
        }
        throw new MissingClientContextException();
    }
}
