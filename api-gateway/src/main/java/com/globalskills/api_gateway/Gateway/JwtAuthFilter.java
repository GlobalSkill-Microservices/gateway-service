package com.globalskills.api_gateway.Gateway;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthFilter implements GatewayFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .flatMap(principal -> {
                    if (principal instanceof JwtAuthenticationToken) {
                        JwtAuthenticationToken token = (JwtAuthenticationToken) principal;
                        Jwt jwt = token.getToken();
                        String userIdString = jwt.getSubject();

                        if (userIdString != null) {
                            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                    .headers(httpHeaders -> httpHeaders.remove("X-User-ID"))
                                    .header("X-User-ID", userIdString)
                                    .build();

                            ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
                            return chain.filter(mutatedExchange);
                        }
                    }
                    return chain.filter(exchange);
                })
                .switchIfEmpty(chain.filter(exchange));
    }


    @Override
    public int getOrder() {
        return 1;
    }
}
