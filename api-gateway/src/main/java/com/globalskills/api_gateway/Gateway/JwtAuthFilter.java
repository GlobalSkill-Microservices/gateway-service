package com.globalskills.api_gateway.Gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final static Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.debug("🔐 JwtAuthFilter triggered for path: {}", exchange.getRequest().getPath());

        return exchange.getPrincipal()
                .doOnNext(principal -> log.debug("👤 Principal received: {}", principal.getClass().getSimpleName()))
                .flatMap(principal -> {
                    if (principal instanceof JwtAuthenticationToken) {
                        JwtAuthenticationToken token = (JwtAuthenticationToken) principal;
                        Jwt jwt = token.getToken();

                        log.debug("📄 JWT claims: {}", jwt.getClaims());
                        log.debug("🔍 Extracted subject (user ID): {}", jwt.getSubject());

                        String userIdString = jwt.getSubject();

                        if (userIdString != null && !userIdString.isBlank()) {
                            log.info("✅ Valid JWT subject found: {} — attaching to header X-User-ID", userIdString);

                            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                    .headers(httpHeaders -> {
                                        httpHeaders.remove("X-User-ID");
                                        httpHeaders.add("X-User-ID", userIdString);
                                    })
                                    .build();

                            ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

                            return chain.filter(mutatedExchange);
                        } else {
                            log.warn("⚠️ JWT subject is null or blank — cannot attach X-User-ID");
                        }
                    } else {
                        log.warn("⚠️ Principal is not an instance of JwtAuthenticationToken — skipping header injection");
                    }

                    return chain.filter(exchange);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("⚠️ No principal found — proceeding without X-User-ID header");
                    return chain.filter(exchange);
                }));

    }


    @Override
    public int getOrder() {
        return 1;
    }
}
