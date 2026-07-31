-- WP-0 shared foundations (docs/14 §4). UUIDv7 PKs are assigned in-application by Hibernate;
-- pgcrypto is enabled defensively so seed migrations (V3, V10, ...) can use gen_random_uuid()
-- in raw SQL inserts.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Append-only audit trail (docs/09 §6, ADR-007, BR-SE-002). No FK to domain tables: audit must
-- outlive archived data, and there is no UPDATE/DELETE path onto this table anywhere.
CREATE TABLE audit_log (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    occurred_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    actor_account_id  UUID,
    action            TEXT NOT NULL,
    entity_type       TEXT NOT NULL,
    entity_id         TEXT,
    summary           JSONB,
    ip                TEXT
);

CREATE INDEX idx_audit_log_entity_type_entity_id ON audit_log (entity_type, entity_id);
CREATE INDEX idx_audit_log_occurred_at ON audit_log (occurred_at);
