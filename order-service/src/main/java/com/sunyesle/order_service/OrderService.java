package com.sunyesle.order_service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunyesle.inventory_service.event.StockUpdatedEvent;
import com.sunyesle.inventory_service.event.StockUpdatedStatus;
import com.sunyesle.order_service.event.OrderPlacedEvent;
import com.sunyesle.order_service.outbox.Outbox;
import com.sunyesle.order_service.outbox.OutboxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final InventoryClient inventoryClient;
    private final ObjectMapper avroObjectMapper;

    public OrderService(
            OrderRepository orderRepository,
            OutboxRepository outboxRepository,
            InventoryClient inventoryClient,
            @Qualifier("avroObjectMapper") ObjectMapper avroObjectMapper) {
        this.orderRepository = orderRepository;
        this.outboxRepository = outboxRepository;
        this.inventoryClient = inventoryClient;
        this.avroObjectMapper = avroObjectMapper;
    }

    @Transactional
    public void placeOrder(OrderRequest orderRequest) {
        boolean isProductInStock = inventoryClient.isInStock(orderRequest.skuCode(), orderRequest.quantity());
        if (!isProductInStock) {
            throw new RuntimeException("Product with SkuCode " + orderRequest.skuCode() + " is not in stock");
        }

        Order order = new Order(
                UUID.randomUUID().toString(),
                orderRequest.skuCode(),
                orderRequest.price(),
                orderRequest.quantity()
        );
        orderRepository.save(order);

        OrderPlacedEvent event = new OrderPlacedEvent(
                order.getOrderNumber(),
                orderRequest.userDetails().email(),
                orderRequest.userDetails().firstName(),
                orderRequest.userDetails().lastName(),
                order.getSkuCode(),
                order.getQuantity()
        );
        try {
            String payload = avroObjectMapper.writeValueAsString(event);
            Outbox outbox = new Outbox(
                    order.getOrderNumber(),
                    "order-placed",
                    payload
            );
            outboxRepository.save(outbox);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Serialization failed", e);
        }
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
