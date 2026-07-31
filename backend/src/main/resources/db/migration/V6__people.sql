-- WP-3 people (docs/14 §4, FR-STU-01/02/05, FR-STF-01/02, BR-EN-002/004, BR-ST-001/002).

CREATE TABLE students (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_number  TEXT NOT NULL,
    first_name      TEXT NOT NULL,
    last_name       TEXT NOT NULL,
    other_names     TEXT,
    date_of_birth   DATE NOT NULL,
    gender          TEXT NOT NULL CHECK (gender IN ('MALE', 'FEMALE')),
    photo_path      TEXT,
    admission_date  DATE NOT NULL,
    status          TEXT NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('APPLICANT', 'ACTIVE', 'TRANSFERRED_OUT', 'WITHDRAWN', 'GRADUATED', 'DECEASED')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,
    updated_by      UUID,
    archived_at     TIMESTAMPTZ,
    version         BIGINT NOT NULL DEFAULT 0
);

-- BR-EN-002: immutable, unique student number, format UBS-<entryYear>-<sequence> (A-05) —
-- enforced at generation time in the service layer; uniqueness is the schema's job.
CREATE UNIQUE INDEX uq_students_student_number ON students (student_number) WHERE archived_at IS NULL;

CREATE TABLE guardians (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name  TEXT NOT NULL,
    last_name   TEXT NOT NULL,
    phone       TEXT NOT NULL,
    email       TEXT,
    occupation  TEXT,
    address     TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID,
    updated_by  UUID,
    archived_at TIMESTAMPTZ,
    version     BIGINT NOT NULL DEFAULT 0
);

-- BR-EN-004: every ACTIVE student needs >= 1 linked guardian, >= 1 of them flagged primary
-- contact. Relationship type comes from the link, not the person (docs/02 §2 People).
CREATE TABLE student_guardians (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id                  UUID NOT NULL REFERENCES students (id),
    guardian_id                 UUID NOT NULL REFERENCES guardians (id),
    relationship_type           TEXT NOT NULL
        CHECK (relationship_type IN ('MOTHER', 'FATHER', 'GRANDPARENT', 'AUNT_UNCLE', 'SIBLING', 'OTHER')),
    is_primary_contact          BOOLEAN NOT NULL DEFAULT FALSE,
    has_custody                 BOOLEAN NOT NULL DEFAULT TRUE,
    receives_billing            BOOLEAN NOT NULL DEFAULT FALSE,
    receives_academic_reports   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at                  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by                  UUID,
    updated_by                  UUID,
    archived_at                 TIMESTAMPTZ,
    version                     BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uq_student_guardians_student_id_guardian_id
    ON student_guardians (student_id, guardian_id) WHERE archived_at IS NULL;
CREATE INDEX idx_student_guardians_guardian_id ON student_guardians (guardian_id) WHERE archived_at IS NULL;

-- Attachment metadata only — the file itself lives outside the DB (docs/11 §4); storage target
-- decided at WP-3 per docs/14 §8: filesystem behind a DocumentStorage seam, object storage later
-- is an additive swap. Scoped to students: STUDENT_DOCUMENT_VIEW/UPLOAD are the only document
-- permissions in the catalog (docs/14 §6) — no staff-document permission exists yet.
CREATE TABLE student_documents (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id     UUID NOT NULL REFERENCES students (id),
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

CREATE INDEX idx_student_documents_student_id ON student_documents (student_id) WHERE archived_at IS NULL;

CREATE TABLE staff (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    staff_number           TEXT NOT NULL,
    first_name             TEXT NOT NULL,
    last_name              TEXT NOT NULL,
    staff_type             TEXT NOT NULL CHECK (staff_type IN ('TEACHING', 'NON_TEACHING')),
    ges_registration_number TEXT,
    employment_start_date  DATE NOT NULL,
    employment_end_date    DATE,
    status                 TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'ENDED')),
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by             UUID,
    updated_by             UUID,
    archived_at            TIMESTAMPTZ,
    version                BIGINT NOT NULL DEFAULT 0
);

-- BR-ST-001: immutable, unique staff number.
CREATE UNIQUE INDEX uq_staff_staff_number ON staff (staff_number) WHERE archived_at IS NULL;

CREATE TABLE staff_qualifications (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    staff_id        UUID NOT NULL REFERENCES staff (id),
    qualification   TEXT NOT NULL,
    institution     TEXT,
    year_obtained   INTEGER,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,
    updated_by      UUID,
    archived_at     TIMESTAMPTZ,
    version         BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_staff_qualifications_staff_id ON staff_qualifications (staff_id) WHERE archived_at IS NULL;
