CREATE TABLE transfers
(
    id         BIGSERIAL PRIMARY KEY,
    from_login VARCHAR(64)              NOT NULL,
    to_login   VARCHAR(64)              NOT NULL,
    amount     BIGINT                   NOT NULL CHECK (amount > 0),
    status     VARCHAR(32)              NOT NULL CHECK (status IN ('COMPLETED', 'FAILED', 'COMPENSATION_FAILED')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_transfers_from_login ON transfers (from_login, created_at);
CREATE INDEX idx_transfers_to_login ON transfers (to_login, created_at);

CREATE TABLE outbox_notifications
(
    id         BIGSERIAL PRIMARY KEY,
    login      VARCHAR(64)              NOT NULL,
    message    VARCHAR(1024)            NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    sent_at    TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_outbox_notifications_unsent ON outbox_notifications (id) WHERE sent_at IS NULL;
