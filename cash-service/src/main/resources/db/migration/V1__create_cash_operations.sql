CREATE TABLE cash_operations
(
    id         BIGSERIAL PRIMARY KEY,
    login      VARCHAR(64)              NOT NULL,
    action     VARCHAR(16)              NOT NULL CHECK (action IN ('DEPOSIT', 'WITHDRAW')),
    amount     BIGINT                   NOT NULL CHECK (amount > 0),
    balance    BIGINT                   NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_cash_operations_login ON cash_operations (login, created_at);

CREATE TABLE outbox_notifications
(
    id         BIGSERIAL PRIMARY KEY,
    login      VARCHAR(64)              NOT NULL,
    message    VARCHAR(1024)            NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    sent_at    TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_outbox_notifications_unsent ON outbox_notifications (id) WHERE sent_at IS NULL;
