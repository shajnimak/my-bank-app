CREATE TABLE accounts
(
    id        BIGSERIAL PRIMARY KEY,
    login     VARCHAR(64)  NOT NULL UNIQUE,
    name      VARCHAR(255) NOT NULL,
    birthdate DATE         NOT NULL,
    balance   BIGINT       NOT NULL DEFAULT 0 CHECK (balance >= 0)
);

CREATE TABLE outbox_notifications
(
    id         BIGSERIAL PRIMARY KEY,
    login      VARCHAR(64)              NOT NULL,
    message    VARCHAR(1024)            NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    sent_at    TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_outbox_notifications_unsent ON outbox_notifications (id) WHERE sent_at IS NULL;
