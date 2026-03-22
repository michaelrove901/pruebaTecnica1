package com.example.funds.infrastructure.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthenticatedClientUser implements UserDetails {

    private final String clientId;
    private final String email;
    private final String passwordHash;
    private final List<GrantedAuthority> authorities;

    public AuthenticatedClientUser(String clientId, String email, String passwordHash, List<String> roles) {
        this.clientId = clientId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .map(GrantedAuthority.class::cast)
                .toList();
    }

    public String clientId() {
        return clientId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
