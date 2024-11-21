package com.roman.web.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

public class GlobalExceptionHandler implements WebExceptionHandler {
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        Mono<ServerResponse> response = ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .bodyValue("Произошла ошибка: " + ex.getMessage());

        return null;
    }
}
