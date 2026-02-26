package com.sunyesle.order_service.outbox.retry;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OutboxRetryPolicyConfig {
    private final OutboxRetryProperties properties;

    @Bean
    public OutboxRetryPolicy retryPolicy() {
        return switch (properties.getPolicy().toLowerCase()) {
            case "fixed" -> new FixedRetryPolicy(properties.getFixed().getDelay());
            case "linear" -> {
                OutboxRetryProperties.Linear linear = properties.getLinear();
                yield new LinearRetryPolicy(
                        linear.getInitialDelay(),
                        linear.getMaxDelay(),
                        linear.getIncrement()
                );
            }
            case "exponential" -> {
                OutboxRetryProperties.Exponential exponential = properties.getExponential();
                yield new ExponentialRetryPolicy(
                        exponential.getInitialDelay(),
                        exponential.getMaxDelay(),
                        exponential.getMultiplier()
                );
            }
            default -> throw new IllegalArgumentException("Unknown policy: " + properties.getPolicy());
        };
    }
}
