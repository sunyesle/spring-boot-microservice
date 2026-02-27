package com.sunyesle.order_service;

import com.sunyesle.inventory_service.event.StockUpdatedEvent;
import com.sunyesle.inventory_service.event.StockUpdatedStatus;
import com.sunyesle.order_service.event.OrderPlacedEvent;
import com.sunyesle.order_service.inventory.InventoryClient;
import com.sunyesle.order_service.inventory.InventoryResponse;
import com.sunyesle.order_service.outbox.OutboxEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public void placeOrder(OrderRequest orderRequest) {
        InventoryResponse inventory = inventoryClient.getStock(orderRequest.skuCode());
        if (inventory.quantity() < orderRequest.quantity()) {
            throw new RuntimeException("Product with SkuCode " + orderRequest.skuCode() + " is not in stock");
        }

        Order order = new Order(
                UUID.randomUUID().toString(),
                orderRequest.skuCode(),
                orderRequest.price(),
                orderRequest.quantity()
        );
        orderRepository.save(order);

        OrderPlacedEvent payload = new OrderPlacedEvent(
                order.getOrderNumber(),
                orderRequest.userDetails().email(),
                orderRequest.userDetails().firstName(),
                orderRequest.userDetails().lastName(),
                order.getSkuCode(),
                order.getQuantity()
        );
        OutboxEvent event = new OutboxEvent(order.getOrderNumber(), "order-placed", payload);
        publisher.publishEvent(event);
    }

    @KafkaListener(topics = "stock-updated")
    @Transactional
    public void listen(StockUpdatedEvent event) {
        log.info("Got Message from stock-updated topic {}", event);
        Order order = orderRepository.findByOrderNumber(event.getOrderNumber())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        switch (event.getStatus()) {
            case StockUpdatedStatus.SUCCESS -> {
                order.confirm();
                log.info("Order {} has been CONFIRMED", event.getOrderNumber());
            }
            case StockUpdatedStatus.FAILURE -> {
                order.cancel();
                log.warn("Order {} has been CANCELLED due to inventory failure", event.getOrderNumber());
            }
        }
    }
}
