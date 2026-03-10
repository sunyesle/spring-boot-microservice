package com.sunyesle.order_service.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunyesle.order_service.event.OrderPlacedEvent;
import com.sunyesle.order_service.outbox.retry.OutboxRetryPolicy;
import com.sunyesle.order_service.outbox.retry.OutboxRetryProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class OutboxProcessor {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OutboxService outboxService;
    private final ObjectMapper avroObjectMapper;
    private final OutboxRetryPolicy outboxRetryPolicy;
    private final OutboxRetryProperties outboxRetryProperties;

    public OutboxProcessor(
            KafkaTemplate<String, Object> kafkaTemplate,
            OutboxService outboxService,
            @Qualifier("avroObjectMapper") ObjectMapper avroObjectMapper,
            OutboxRetryPolicy outboxRetryPolicy,
            OutboxRetryProperties outboxRetryProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.outboxService = outboxService;
        this.avroObjectMapper = avroObjectMapper;
        this.outboxRetryPolicy = outboxRetryPolicy;
        this.outboxRetryProperties = outboxRetryProperties;
    }

    public void processAll() {
        outboxService.getReadyOutboxes().forEach(this::processInternal);
    }

    public void process(long outboxId) {
        processInternal(outboxService.getOutbox(outboxId));
    }

    private void processInternal(Outbox outbox) {
        try {
            log.info("Outbox {} sending: attempt {}", outbox.getId(), outbox.getFailureCount() + 1);

            Object data = convertToObject(outbox.getEventType(), outbox.getPayload());
            kafkaTemplate.send(outbox.getEventType(), outbox.getAggregateId(), data)
                    .get(5, TimeUnit.SECONDS);

            outboxService.markCompleted(outbox.getId());
            log.info("Outbox {} marked as COMPLETED", outbox.getId());
        } catch (Exception e) {
            log.error("Outbox {} failed: {}", outbox.getId(), e.getMessage());

            if (outbox.getFailureCount() >= outboxRetryProperties.getMaxRetries()) {
                outboxService.markFailed(outbox.getId());

                log.error("Outbox {} marked as FAILED", outbox.getId());
            } else {
                long delayMillis = outboxRetryPolicy.calculateDelay(outbox.getFailureCount());
                outboxService.scheduleNextRetry(outbox.getId(), Duration.ofMillis(delayMillis));

                log.warn("Outbox {} retry scheduled: {}ms", outbox.getId(), delayMillis);
            }
        }
    }

    private Object convertToObject(String eventType, String jsonPayload) throws JsonProcessingException {
        return switch (eventType) {
            case "order-placed" -> avroObjectMapper.readValue(jsonPayload, OrderPlacedEvent.class);
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }
}
