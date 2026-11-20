
CREATE TABLE billing_refunds (
    id BIGINT NOT NULL AUTO_INCREMENT,
    amount DECIMAL(12,2) NOT NULL,
    booking_reference BINARY(16) NOT NULL,
    razorpay_payment_id VARCHAR(100) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    refund_reference VARCHAR(100) NULL,
    idempotency_key VARCHAR(100) NOT NULL,
    status ENUM('PENDING', 'SUCCEEDED', 'FAILED') NOT NULL,
    failure_reason VARCHAR(1000) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_billing_refund_booking (booking_reference),
    UNIQUE KEY uk_billing_refund_reference (refund_reference),
    UNIQUE KEY uk_billing_refund_idempotency (idempotency_key)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;
