CREATE TABLE IF NOT EXISTS pre_approved (
    id               VARCHAR(32)   PRIMARY KEY,
    customer_id      VARCHAR(32)   NOT NULL,
    status           VARCHAR(16)   NOT NULL,
    available_amount NUMERIC(18,2) NOT NULL,
    CONSTRAINT ck_pre_approved_available_amount CHECK (available_amount >= 0)
);

CREATE TABLE IF NOT EXISTS usage_requests (
    id                BIGSERIAL     PRIMARY KEY,
    request_reference VARCHAR(64)   NOT NULL,
    pre_approved_id   VARCHAR(32)   NOT NULL,
    customer_id       VARCHAR(32)   NOT NULL,
    amount            NUMERIC(18,2) NOT NULL,
    status            VARCHAR(16)   NOT NULL,
    reason_code       VARCHAR(32),
    processed_at      TIMESTAMPTZ   NOT NULL,
    CONSTRAINT uq_usage_requests_reference UNIQUE (request_reference)
);

CREATE INDEX IF NOT EXISTS idx_usage_requests_processed_at ON usage_requests (processed_at DESC, id DESC);
