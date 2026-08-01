-- WP-11: School Configuration & Staff Documents — two of the three genuine gaps found in the
-- WP-1..WP-10 backend audit (the third, Student admission-record fields, was independently added
-- by V17__student_admission_fields — same column set, already in place, nothing to redo here).
-- Deliberately excludes medical information anywhere in this WP: health data is ring-fenced to the
-- (post-MVP) health module per BR-HE-001, never on the core Student record.

CREATE TABLE staff_documents (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    staff_id       UUID NOT NULL REFERENCES staff (id),
    document_type  TEXT NOT NULL,
    storage_key    TEXT NOT NULL,
    original_name  TEXT NOT NULL,
    content_type   TEXT NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by     UUID,
    updated_by     UUID,
    archived_at    TIMESTAMPTZ,
    version        BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_staff_documents_staff_id ON staff_documents (staff_id) WHERE archived_at IS NULL;

CREATE TABLE school_settings (
    id                           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_name                 TEXT NOT NULL,
    motto                       TEXT,
    address                      TEXT,
    contact_email                TEXT,
    contact_phone                TEXT,
    logo_storage_key             TEXT,
    sms_notifications_enabled    BOOLEAN NOT NULL DEFAULT TRUE,
    email_notifications_enabled  BOOLEAN NOT NULL DEFAULT TRUE,
    created_at                   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at                   TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by                   UUID,
    updated_by                   UUID,
    archived_at                  TIMESTAMPTZ,
    version                      BIGINT NOT NULL DEFAULT 0
);

-- At most one non-archived row, ever — a partial unique index on a constant expression.
CREATE UNIQUE INDEX uq_school_settings_singleton ON school_settings ((true)) WHERE archived_at IS NULL;

INSERT INTO school_settings (school_name, address, contact_email)
VALUES ('University Basic School, Legon', 'Legon, Accra, Ghana', 'info@ubs.edu.gh');
