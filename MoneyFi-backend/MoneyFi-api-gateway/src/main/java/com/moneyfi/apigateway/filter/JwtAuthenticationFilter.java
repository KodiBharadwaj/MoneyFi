package com.moneyfi.apigateway.filter;

import com.moneyfi.apigateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (path.contains("api/v1/user-service/open") || path.contains("api/v1/user-service/Oauth")
                || path.contains("api/v1/user-service/auth") || path.contains("api/v1/user-service/sse-notifications/subscribe")
                || path.contains("/v3/api-docs") || path.contains("/swagger-ui") || path.contains("/actuator/**")) {
            return chain.filter(exchange);
        }
        List<String> headers = exchange.getRequest().getHeaders().get("Authorization");
        if (headers == null || headers.isEmpty()) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        String token = headers.get(0).replace("Bearer ", "");
        try {
            Claims claims = jwtUtil.validateToken(token);
            ServerHttpRequest request = exchange.getRequest().mutate().header("userId", claims.getSubject()).build();
            return chain.filter(exchange.mutate().request(request).build());
        } catch (Exception e) {
            e.printStackTrace();
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}
