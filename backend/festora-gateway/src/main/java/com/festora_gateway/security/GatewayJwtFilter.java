
package com.festora_gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Set;

@Component
public class GatewayJwtFilter implements GlobalFilter, Ordered {

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/auth/register",
            "/api/auth/register/organizer",
            "/api/auth/login"
    );

    private final SecretKey signingKey;

    public GatewayJwtFilter(
            @Value("${festora.jwt.secret}") String secret
    ) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must contain at least 256 bits"
            );
        }

        signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            org.springframework.cloud.gateway.filter.GatewayFilterChain chain
    ) {

        String path = exchange.getRequest()
                .getURI()
                .getPath();

        HttpMethod method = exchange.getRequest().getMethod();

        // Allow CORS preflight requests.
        if (method == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        // Allow public authentication endpoints.
        if (PUBLIC_PATHS.contains(path)) {
            return chain.filter(exchange);
        }

        // Allow public event discovery and event details.
        if (method == HttpMethod.GET
                && (path.equals("/api/events")
                || path.startsWith("/api/events/"))) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authorization == null
                || !authorization.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authorization.substring(7);

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String subject = claims.getSubject();
            String role = claims.get("role", String.class);

            if (subject == null
                    || !subject.matches("[1-9][0-9]*")
                    || role == null
                    || !Set.of("USER", "ORGANIZER", "ADMIN")
                    .contains(role)) {
                return unauthorized(exchange);
            }

            return chain.filter(exchange);

        } catch (RuntimeException exception) {
            return unauthorized(exchange);
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {

        exchange.getResponse().setStatusCode(
                HttpStatus.UNAUTHORIZED
        );

        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
