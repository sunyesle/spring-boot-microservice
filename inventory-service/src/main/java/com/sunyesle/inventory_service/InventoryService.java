package com.sunyesle.inventory_service;

import com.sunyesle.inventory_service.event.StockUpdatedEvent;
import com.sunyesle.inventory_service.event.StockUpdatedStatus;
import com.sunyesle.order_service.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final KafkaTemplate<String, StockUpdatedEvent> kafkaTemplate;

    @Transactional
    public InventoryResponse addStock(InventoryRequest request) {
        Inventory inventory = new Inventory(request.skuCode(), request.quantity());
        inventoryRepository.save(inventory);
        log.info("Inventory created successfully");
        return new InventoryResponse(inventory.getId(), inventory.getSkuCode(), inventory.getQuantity());
    }

    @Transactional(readOnly = true)
    public InventoryResponse getStock(String skuCode) {
        Inventory inventory = inventoryRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));
        return new InventoryResponse(inventory.getId(), inventory.getSkuCode(), inventory.getQuantity());
    }

    @Transactional(readOnly = true)
    public boolean isInStock(String skuCode, Integer quantity) {
        return inventoryRepository.existsBySkuCodeAndQuantityIsGreaterThanEqual(skuCode, quantity);
    }

    @KafkaListener(topics = "order-placed")
    @Transactional
    public void decreaseStock(OrderPlacedEvent orderPlacedEvent) {
        log.info("Got Message from order-placed topic {}", orderPlacedEvent);
        try {
            Inventory inventory = inventoryRepository.findBySkuCodeWithLock(orderPlacedEvent.getSkuCode())
                    .orElseThrow(() -> new RuntimeException("Inventory not found"));

            inventory.decrease(orderPlacedEvent.getQuantity());

            publishEvent(orderPlacedEvent.getOrderNumber(), StockUpdatedStatus.SUCCESS);

            log.info("Stock updated successfully");
        } catch (Exception e) {
            publishEvent(orderPlacedEvent.getOrderNumber(), StockUpdatedStatus.FAILURE);
            log.info("Stock update failed. reason: {}", e.getMessage());
        }
    }

    private void publishEvent(String orderNumber, StockUpdatedStatus status) {
        kafkaTemplate.send("stock-updated", orderNumber, new StockUpdatedEvent(orderNumber, status));
    }
}
