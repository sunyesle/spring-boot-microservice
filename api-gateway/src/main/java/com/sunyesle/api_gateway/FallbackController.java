package com.sunyesle.api_gateway;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class FallbackController {

    @GetMapping("/fallbackRoute")
    public Mono<Void> fallback(ServerWebExchange exchange) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable");
    }
}
