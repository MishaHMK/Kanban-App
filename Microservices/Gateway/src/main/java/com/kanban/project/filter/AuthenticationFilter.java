package com.kanban.project.filter;

import com.kanban.project.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationFilter implements GlobalFilter {
    private static final List<String> PUBLIC_PATHS = List.of("/auth", "/ws-endpoint");
    private static final String BEARER_HEADER = "Bearer ";
    private static final String USER_ID_HEADER = "X-User-Id";

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        log.info("Gateway filter hit: {}", path);

        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            log.info("Passing through public path: {}", path);
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_HEADER)) {
            return onError(exchange);
        }

        String token = authHeader.substring(BEARER_HEADER.length());

        if (!jwtUtil.isTokenValid(token)) {
            return onError(exchange);
        }

        String userId = jwtUtil.extractAllClaims(token).getSubject();

        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
               .header(USER_ID_HEADER, userId)
               .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private Mono<Void> onError(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}