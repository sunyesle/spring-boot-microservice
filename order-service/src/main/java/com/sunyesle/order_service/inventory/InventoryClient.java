package com.sunyesle.order_service.inventory;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface InventoryClient {

    Logger log = LoggerFactory.getLogger(InventoryClient.class);

    @GetExchange("/api/inventory")
    @CircuitBreaker(name = "inventory", fallbackMethod = "isInStockFallback")
    @Retry(name = "inventory")
    boolean isInStock(@RequestParam String skuCode, @RequestParam Integer quantity);

    @GetExchange("/api/inventory/{skuCode}")
    @CircuitBreaker(name = "inventory", fallbackMethod = "getStockFallback")
    @Retry(name = "inventory")
    InventoryResponse getStock(@PathVariable String skuCode);

    default boolean isInStockFallback(String skuCode, Integer quantity, Throwable throwable) {
        log.info("Cannot get inventory for skuCode {}, failure reason: {}", skuCode, throwable.getMessage());
        return false;
    }

    default InventoryResponse getStockFallback(String skuCode, Throwable throwable) {
        log.info("Cannot get inventory for skuCode {}, failure reason: {}", skuCode, throwable.getMessage());
        return new InventoryResponse(0L, skuCode, 0);
    }
}
