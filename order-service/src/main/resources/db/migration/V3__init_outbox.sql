CREATE TABLE `outbox`
(
    `id`            bigint(20)   NOT NULL AUTO_INCREMENT,
    `aggregate_id`  VARCHAR(255) NOT NULL,
    `event_type`    VARCHAR(255) NOT NULL,
    `payload`       TEXT         NOT NULL,
    `created_at`    DATETIME(6)  NOT NULL,
    `processed_at`  DATETIME(6)  NULL,
    PRIMARY KEY (`id`)
);
