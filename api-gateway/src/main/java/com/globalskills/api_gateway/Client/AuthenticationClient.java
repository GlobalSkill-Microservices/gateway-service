package com.globalskills.api_gateway.Client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "user-service")
public interface AuthenticationClient {


}
