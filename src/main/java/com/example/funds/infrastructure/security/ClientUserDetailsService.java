package com.example.funds.infrastructure.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.funds.domain.port.out.ClientRepositoryPort;

@Service
public class ClientUserDetailsService implements UserDetailsService {

    private final ClientRepositoryPort clientRepositoryPort;

    public ClientUserDetailsService(ClientRepositoryPort clientRepositoryPort) {
        this.clientRepositoryPort = clientRepositoryPort;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return clientRepositoryPort.findByEmail(username)
                .map(client -> new AuthenticatedClientUser(
                        client.clientId(),
                        client.email(),
                        client.passwordHash(),
                        client.roles().stream().map(Enum::name).toList()
                ))
                .orElseThrow(() -> new UsernameNotFoundException("Client not found for email: " + username));
    }
}
