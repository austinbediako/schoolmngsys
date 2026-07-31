-- WP-7 finance (docs/14 §4, BR-FI-001..007, FR-FIN-01..06). Money is GHS numeric(12,2) per
-- CLAUDE.md convention. Arrears carry-forward (BR-FI-005) is deliberately NOT a duplicated line
-- item: a student's older unpaid invoices simply stay open, and payment allocation always attacks
-- the oldest open invoice first (BR-FI-002/A-09) — that ordering *is* the carry-forward mechanism.

CREATE TABLE fee_schedules (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_level_id UUID NOT NULL REFERENCES class_levels (id),
    term_id        UUID NOT NULL REFERENCES terms (id),
    status         TEXT NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'APPROVED')),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by     UUID,
    updated_by     UUID,
    archived_at    TIMESTAMPTZ,
    version        BIGINT NOT NULL DEFAULT 0
);

-- BR-FI-001: one schedule per (level, term).
CREATE UNIQUE INDEX uq_fee_schedules_class_level_id_term_id
    ON fee_schedules (class_level_id, term_id) WHERE archived_at IS NULL;

CREATE TABLE fee_items (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fee_schedule_id  UUID NOT NULL REFERENCES fee_schedules (id),
    name             TEXT NOT NULL,
    amount           NUMERIC(12, 2) NOT NULL,
    mandatory        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by       UUID,
    updated_by       UUID,
    archived_at      TIMESTAMPTZ,
    version          BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_fee_items_amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_fee_items_fee_schedule_id ON fee_items (fee_schedule_id) WHERE archived_at IS NULL;

-- Per (Student, Term); status is a maintained cache recomputed on every allocation change.
CREATE TABLE invoices (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    enrollment_id  UUID NOT NULL REFERENCES enrollments (id),
    term_id        UUID NOT NULL REFERENCES terms (id),
    status         TEXT NOT NULL DEFAULT 'ISSUED' CHECK (status IN ('ISSUED', 'PART_PAID', 'PAID')),
    issued_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by     UUID,
    updated_by     UUID,
    archived_at    TIMESTAMPTZ,
    version        BIGINT NOT NULL DEFAULT 0
);

-- BR-EN-001-style: one invoice per student per term.
CREATE UNIQUE INDEX uq_invoices_enrollment_id_term_id ON invoices (enrollment_id, term_id) WHERE archived_at IS NULL;
CREATE INDEX idx_invoices_enrollment_id ON invoices (enrollment_id) WHERE archived_at IS NULL;

-- Append-only: a fee-item charge, or an approved adjustment's reduction — never edited once billed.
CREATE TABLE invoice_lines (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id   UUID NOT NULL REFERENCES invoices (id),
    description  TEXT NOT NULL,
    amount       NUMERIC(12, 2) NOT NULL,
    source_type  TEXT NOT NULL CHECK (source_type IN ('FEE_ITEM', 'ADJUSTMENT')),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by   UUID,
    updated_by   UUID,
    archived_at  TIMESTAMPTZ,
    version      BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_invoice_lines_invoice_id ON invoice_lines (invoice_id) WHERE archived_at IS NULL;

-- BR-FI-004: a student-specific price change, never a schedule/line edit. Takes effect (becomes an
-- invoice_line) only once APPROVED.
CREATE TABLE adjustments (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id   UUID NOT NULL REFERENCES invoices (id),
    type         TEXT NOT NULL CHECK (type IN ('DISCOUNT', 'SCHOLARSHIP', 'WAIVER')),
    amount       NUMERIC(12, 2) NOT NULL,
    reason       TEXT NOT NULL,
    status       TEXT NOT NULL DEFAULT 'PROPOSED' CHECK (status IN ('PROPOSED', 'APPROVED', 'REJECTED')),
    invoice_line_id UUID REFERENCES invoice_lines (id),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by   UUID,
    updated_by   UUID,
    archived_at  TIMESTAMPTZ,
    version      BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_adjustments_amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_adjustments_invoice_id ON adjustments (invoice_id) WHERE archived_at IS NULL;

-- BR-FI-003: immutable once posted. amount/channel/reference/receipt_number are never updated —
-- enforced application-only (docs/14 §8), the same pattern as every other "immutable" entity here.
CREATE TABLE payments (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    enrollment_id        UUID NOT NULL REFERENCES enrollments (id),
    amount               NUMERIC(12, 2) NOT NULL,
    channel              TEXT NOT NULL CHECK (channel IN ('CASH', 'BANK', 'CHEQUE', 'MOMO')),
    reference            TEXT,
    receipt_number       TEXT NOT NULL,
    reversed             BOOLEAN NOT NULL DEFAULT FALSE,
    reversal_of_payment_id UUID REFERENCES payments (id),
    reversal_reason      TEXT,
    allocation_override_reason TEXT,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by           UUID,
    updated_by           UUID,
    archived_at          TIMESTAMPTZ,
    version               BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_payments_amount_nonzero CHECK (amount <> 0)
);

CREATE UNIQUE INDEX uq_payments_receipt_number ON payments (receipt_number);
CREATE INDEX idx_payments_enrollment_id ON payments (enrollment_id);

CREATE TABLE payment_allocations (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id   UUID NOT NULL REFERENCES payments (id),
    invoice_id   UUID NOT NULL REFERENCES invoices (id),
    amount       NUMERIC(12, 2) NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by   UUID,
    updated_by   UUID,
    archived_at  TIMESTAMPTZ,
    version      BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_payment_allocations_amount_nonzero CHECK (amount <> 0)
);

CREATE INDEX idx_payment_allocations_payment_id ON payment_allocations (payment_id);
CREATE INDEX idx_payment_allocations_invoice_id ON payment_allocations (invoice_id);
