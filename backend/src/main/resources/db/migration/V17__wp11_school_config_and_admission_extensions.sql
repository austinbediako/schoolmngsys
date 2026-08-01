-- WP-11: School Configuration & Admission Extensions — three genuine gaps found in the WP-1..WP-10
-- backend audit: no school-settings module, no staff document uploads, and Student was missing
-- admission-record fields (nationality, previous school, address, emergency contact). Deliberately
-- excludes medical information: health data is ring-fenced to the (post-MVP) health module per
-- BR-HE-001, never on the core Student record.

ALTER TABLE students
    ADD COLUMN nationality TEXT,
    ADD COLUMN previous_school TEXT,
    ADD COLUMN residential_address TEXT,
    ADD COLUMN emergency_contact_name TEXT,
    ADD COLUMN emergency_contact_phone TEXT,
    ADD COLUMN emergency_contact_relationship TEXT;

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
