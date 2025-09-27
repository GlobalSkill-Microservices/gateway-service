package com.globalskills.api_gateway.Gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    @Autowired
    ObjectMapper objectMapper;

    @Override
    @NonNull
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        HttpStatus status = resolveHttpStatus(ex);
        response.setStatusCode(status);


        Map<String, Object> errorMap = Map.of(
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", ex.getMessage(),
                "path", exchange.getRequest().getPath().value(),
                "timestamp", Instant.now().toString()
        );

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(errorMap);
        } catch (JsonProcessingException e) {
            bytes = ("{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"Serialization error\"}").getBytes();
        }

        // Trả về dữ liệu lỗi
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    private HttpStatus resolveHttpStatus(Throwable ex) {
        if (ex instanceof ExpiredJwtException) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (ex instanceof MalformedJwtException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (ex instanceof AccessDeniedException) {
            return HttpStatus.FORBIDDEN;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
