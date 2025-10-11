package com.globalskills.api_gateway.Config;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PublicApiConfig {

    List<String> publicApis = List.of(
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/api/user/v3/api-docs",
            "/api/forum/v3/api-docs",
            "/api/authentication/login",
            "/api/authentication/register",
            "/api/authentication/forgot-password"
    );

    public List<String> publicApis() {
        return publicApis;
    }

}
