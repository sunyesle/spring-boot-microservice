package com.sunyesle.order_service.outbox.retry;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "outbox.retry")
public class OutboxRetryProperties {
    private String policy;
    private int maxRetries;
    private Fixed fixed;
    private Linear linear;
    private Exponential exponential;

    @Getter
    @Setter
    public static class Fixed {
        private long delay;
    }

    @Getter
    @Setter
    public static class Linear {
        private long initialDelay;
        private long maxDelay;
        private long increment;
    }

    @Getter
    @Setter
    public static class Exponential {
        private long initialDelay;
        private long maxDelay;
        private double multiplier = 2.0;
    }
}
