package com.example.funds.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtTokenService {

    private static final String ROLES_CLAIM = "roles";
    private static final String EMAIL_CLAIM = "email";

    private final JwtProperties jwtProperties;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateToken(AuthenticatedClientUser user) {
        Instant now = Instant.now();
        Instant expiration = now.plus(jwtProperties.expirationMinutes(), ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(user.clientId())
                .issuer(jwtProperties.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .claim(EMAIL_CLAIM, user.getUsername())
                .claim(
                        ROLES_CLAIM,
                        user.getAuthorities().stream()
                                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                                .toList()
                )
                .signWith(Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    public AuthenticatedClientUser parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8)))
                .requireIssuer(jwtProperties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        List<String> roles = claims.get(ROLES_CLAIM, List.class);
        return new AuthenticatedClientUser(
                claims.getSubject(),
                claims.get(EMAIL_CLAIM, String.class),
                "",
                roles != null ? roles : List.of()
        );
    }
}
