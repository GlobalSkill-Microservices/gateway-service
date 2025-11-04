package com.globalskills.api_gateway.Config;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Getter
@Component
public class PublicApiConfig {

    private final List<String> publicApis = List.of(
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/api/user/v3/api-docs",
            "/api/forum/v3/api-docs",
            "/api/payment/v3/api-docs",
            "/api/booking/v3/api-docs",
            "/api/webhook/**",
            "/api/user-client/**",
            "/api/booking-client/**",
            "/api/authentication/login",
            "/api/authentication/register",
            "/api/authentication/forgot-password"
    );

    private final Map<String, List<String>> publicApiMap = Map.of(
            "GET", List.of(
                    "/api/comment/**",
                    "/api/forum-post",
                    "/api/forum-post/trending-post",
                    "/api/forum-post/shared/{forumPostId}",
                    "/api/post-interaction/**",
                    "/api/user/mentors",
                    "/api/booking/top/mentors"
            )
    );

}
