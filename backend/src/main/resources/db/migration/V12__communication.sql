-- WP-8 Communication module schema (docs/09 §1, docs/14 §4, ADR-008)

CREATE TABLE message_templates (
    id UUID PRIMARY KEY,
    template_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    category VARCHAR(30) NOT NULL,
    subject_template VARCHAR(255),
    body_template TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by UUID,
    archived_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE notification_outbox (
    id UUID PRIMARY KEY,
    template_code VARCHAR(50),
    channel VARCHAR(20) NOT NULL,
    recipient_type VARCHAR(20),
    recipient_id UUID,
    recipient_phone VARCHAR(30),
    recipient_email VARCHAR(255),
    subject VARCHAR(255),
    body TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    max_retries INT NOT NULL DEFAULT 3,
    last_attempt_at TIMESTAMPTZ,
    next_attempt_at TIMESTAMPTZ,
    error_message TEXT,
    provider_name VARCHAR(50),
    provider_reference VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by UUID,
    archived_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_outbox_status_next_attempt ON notification_outbox (status, next_attempt_at) WHERE archived_at IS NULL;

CREATE TABLE notification_deliveries (
    id UUID PRIMARY KEY,
    outbox_id UUID REFERENCES notification_outbox(id),
    channel VARCHAR(20) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    provider_name VARCHAR(50) NOT NULL,
    provider_reference VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    attempted_at TIMESTAMPTZ NOT NULL,
    error_message TEXT,
    cost NUMERIC(12, 2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by UUID,
    archived_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE announcements (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    audience_type VARCHAR(30) NOT NULL,
    target_audience_id UUID,
    author_account_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by UUID,
    archived_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

-- Seed default message templates
INSERT INTO message_templates (
    id, template_code, name, channel, category, subject_template, body_template, active,
    created_at, created_by, updated_at, updated_by, version
) VALUES
(
    '0190ee00-0000-7000-8000-000000000001',
    'RESULT_PUBLISHED',
    'Term Results Published Notification',
    'SMS',
    'ACADEMIC',
    'Term Results Published for {studentName}',
    'Dear {guardianName}, term results for {studentName} in {className} for {termName} have been published.',
    true, NOW(), '00000000-0000-0000-0000-000000000000', NOW(), '00000000-0000-0000-0000-000000000000', 0
),
(
    '0190ee00-0000-7000-8000-000000000002',
    'INVOICE_ISSUED',
    'Fee Invoice Issued Notification',
    'SMS',
    'FINANCIAL',
    'Fee Invoice Issued for {studentName}',
    'Dear {guardianName}, a fee invoice for {studentName} for {termName} has been issued. Total amount: GHS {amount}.',
    true, NOW(), '00000000-0000-0000-0000-000000000000', NOW(), '00000000-0000-0000-0000-000000000000', 0
),
(
    '0190ee00-0000-7000-8000-000000000003',
    'PAYMENT_RECEIPT',
    'Payment Receipt Notification',
    'SMS',
    'FINANCIAL',
    'Payment Receipt {receiptNumber}',
    'Dear {guardianName}, payment of GHS {amount} for {studentName} has been received. Receipt: {receiptNumber}.',
    true, NOW(), '00000000-0000-0000-0000-000000000000', NOW(), '00000000-0000-0000-0000-000000000000', 0
),
(
    '0190ee00-0000-7000-8000-000000000004',
    'ACCOUNT_CREATED',
    'Account Provisioned Notification',
    'SMS',
    'TRANSACTIONAL',
    'UBS-LMIS Account Created',
    'Welcome to UBS-LMIS, {username}. Your account has been provisioned.',
    true, NOW(), '00000000-0000-0000-0000-000000000000', NOW(), '00000000-0000-0000-0000-000000000000', 0
),
(
    '0190ee00-0000-7000-8000-000000000005',
    'ANNOUNCEMENT',
    'School Announcement',
    'SMS',
    'ANNOUNCEMENT',
    '{title}',
    '{content}',
    true, NOW(), '00000000-0000-0000-0000-000000000000', NOW(), '00000000-0000-0000-0000-000000000000', 0
);
