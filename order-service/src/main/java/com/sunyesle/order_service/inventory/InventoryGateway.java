package com.sunyesle.order_service.inventory;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Log4j2
public class InventoryGateway {
    private final InventoryClient inventoryClient;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String STOCK_KEY_PREFIX = "order:stock::";

    public boolean isInStock(String skuCode, int quantity) {
        String key = STOCK_KEY_PREFIX + skuCode;

        try {
            // 캐시 조회
            String cachedStock = redisTemplate.opsForValue().get(key);

            // 캐시 미스 시
            if (cachedStock == null) {
                log.info("Cache miss. skuCode: {}", skuCode);
                // 재고 서비스에서 조회
                Integer stock = inventoryClient.getStock(skuCode).quantity();
                if (stock == null) {
                    return false;
                }

                // 캐시 갱신
                redisTemplate.opsForValue().set(key, stock.toString(), Duration.ofMinutes(1));
                cachedStock = stock.toString();
            }

            // 현재 캐시 기반으로 원자적 차감 수행
            Long remainingStock = redisTemplate.opsForValue().increment(key, -quantity);
            if (remainingStock == null) {
                throw new DataAccessException("Redis increment returned null") {};
            }

            // 수량 체크
            if (remainingStock < 0) {
                redisTemplate.opsForValue().increment(key, quantity);
                return false;
            }
            return true;

        } catch (DataAccessException e) {
            // Redis 관련 에러일 경우 로그를 남기고 재고 서비스 호출하여 수량 체크
            log.error("Redis error for skuCode {}", skuCode, e);
            return inventoryClient.isInStock(skuCode, quantity);
        } catch (Exception e) {
            log.error("Unexpected error for skuCode {}", skuCode, e);
            throw e;
        }
    }
}
