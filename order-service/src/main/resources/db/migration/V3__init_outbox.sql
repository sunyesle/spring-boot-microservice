CREATE TABLE `outbox`
(
    `id`            bigint(20)   NOT NULL AUTO_INCREMENT,
    `aggregate_id`  VARCHAR(255) NOT NULL,
    `event_type`    VARCHAR(255) NOT NULL,
    `payload`       TEXT         NOT NULL,
    `status`        VARCHAR(20)  NOT NULL,
    `created_at`    DATETIME(6)  NOT NULL,
    `completed_at`  DATETIME(6)  NULL,
    `failure_count` INT          NOT NULL,
    `next_retry_at` DATETIME(6)  NOT NULL,
    PRIMARY KEY (`id`)
);
