package com.sunyesle.order_service.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunyesle.order_service.event.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class OutboxProcessor {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OutboxService outboxService;
    private final ObjectMapper avroObjectMapper;

    public OutboxProcessor(
            KafkaTemplate<String, Object> kafkaTemplate,
            OutboxService outboxService,
            @Qualifier("avroObjectMapper") ObjectMapper avroObjectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.outboxService = outboxService;
        this.avroObjectMapper = avroObjectMapper;
    }

    public void processAll() {
        outboxService.getPendingOutboxes().forEach(this::sendMessage);
    }

    private void sendMessage(Outbox outbox) {
        try {
            Object data = convertToObject(outbox.getEventType(), outbox.getPayload());
            kafkaTemplate.send(outbox.getEventType(), outbox.getAggregateId(), data)
                    .get(5, TimeUnit.SECONDS);

            outboxService.complete(outbox.getId());

            log.info("Processed outbox message: {}", outbox.getId());
        } catch (Exception e) {
            log.error("Failed to process outbox message: {}. Reason: {}", outbox.getId(), e.getMessage());
        }
    }

    private Object convertToObject(String eventType, String jsonPayload) throws JsonProcessingException {
        return switch (eventType) {
            case "order-placed" -> avroObjectMapper.readValue(jsonPayload, OrderPlacedEvent.class);
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }
}
