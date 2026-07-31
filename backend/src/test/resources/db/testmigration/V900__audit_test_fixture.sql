-- Test-only fixture table (never shipped): exercises the shared BaseEntity + @Audited aspect
-- against a real table without inventing a real domain entity ahead of its work package.
CREATE TABLE test_widgets (
    id          UUID PRIMARY KEY,
    name        TEXT NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    created_by  UUID,
    updated_by  UUID,
    archived_at TIMESTAMPTZ,
    version     BIGINT NOT NULL DEFAULT 0
);
