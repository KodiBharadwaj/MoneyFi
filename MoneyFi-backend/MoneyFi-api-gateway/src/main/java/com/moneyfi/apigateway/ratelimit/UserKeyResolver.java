package com.moneyfi.apigateway.ratelimit;

import com.moneyfi.apigateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserKeyResolver implements KeyResolver {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just("anonymous");
        }

        try {
            String token = authHeader.substring(7);
            Claims claims = jwtUtil.validateToken(token);
            String userId = String.valueOf(claims.get("userId"));
            return Mono.just(userId);
        } catch (Exception e) {
            return Mono.just("anonymous");
        }
    }
}