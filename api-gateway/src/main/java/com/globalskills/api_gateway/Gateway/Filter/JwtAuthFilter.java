package com.globalskills.api_gateway.Gateway.Filter;

import com.globalskills.api_gateway.Config.PublicApiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class JwtAuthFilter implements WebFilter, Ordered {

    @Autowired
    PublicApiConfig publicApiConfig;

    private final static Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private boolean isPublicApi(String method, String path) {
        boolean isMethodAgnosticPublic = publicApiConfig.getPublicApis().stream()
                .anyMatch(publicPath -> pathMatcher.match(publicPath, path));
        if (isMethodAgnosticPublic) {
            return true;
        }
        String upperCaseMethod = method.toUpperCase();
        Map<String, List<String>> publicMap = publicApiConfig.getPublicApiMap();
        List<String> publicPathsForMethod = publicMap.get(upperCaseMethod);
        if (publicPathsForMethod == null || publicPathsForMethod.isEmpty()) {
            return false;
        }
        return publicPathsForMethod.stream()
                .anyMatch(publicPath -> pathMatcher.match(publicPath, path));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String path = exchange.getRequest().getPath().value();

        String method = exchange.getRequest().getMethod().name();

        if (isPublicApi(method, path)) {
            log.debug("🌐 Public API [{} {}], skipping principal check.", method, path);
            return chain.filter(exchange);
        }
        log.debug("🔐 JwtAuthFilter triggered for path: {}", exchange.getRequest().getPath());

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(authentication -> {
                    if (authentication instanceof JwtAuthenticationToken token) {
                        Jwt jwt = token.getToken();
                        String userIdString = jwt.getSubject();

                        if (userIdString != null && !userIdString.isBlank()) {
                            log.info("✅ Valid JWT subject found: {} — attaching to header X-User-ID", userIdString);

                            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                    .header("X-USER-ID", userIdString)
                                    .build();

                            return chain.filter(exchange.mutate().request(mutatedRequest).build());
                        } else {
                            log.warn("⚠️ JWT subject is null or blank — cannot attach X-User-ID");
                        }
                    } else {
                        log.warn("⚠️ Authentication is null or not JwtAuthenticationToken — skipping header injection");
                    }
                    return chain.filter(exchange);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("⚠️ Security Context is empty — proceeding without X-User-ID header");
                    return chain.filter(exchange);
                }));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
