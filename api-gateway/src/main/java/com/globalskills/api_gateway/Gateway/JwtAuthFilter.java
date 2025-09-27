package com.globalskills.api_gateway.Gateway;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GatewayFilter, Ordered {

    private final WebClient.Builder webClientBuilder;

    private String getToken(ServerWebExchange exchange){
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if(authHeader==null) return null;
        return authHeader.substring(7);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = getToken(exchange);

        if(token == null){
            return Mono.error(new AuthenticationException("Empty Token") {
            });
        }
        return webClientBuilder.build()
                .get()
                .uri("htt//" + token)
                .retrieve()
                .bodyToMono(AccountResponse.class)
                .flatMap(accountResponse -> {
//                    Long accountId = jwtService.extractAccountId(token);
                    Long accountId = accountResponse.getAccountId();
                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .header("X-Account-Id", accountId.toString())
                            .build();
                    ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
                    return chain.filter(mutatedExchange);
                })
                .onErrorResume(ex -> Mono.error(new AuthenticationException(ex.getMessage()) {}));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
