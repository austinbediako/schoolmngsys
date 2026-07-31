-- WP-2 academic structure (docs/14 §4, BR-AS-001..007, FR-ACAD-01..04).

CREATE TABLE academic_years (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    label       TEXT NOT NULL,
    start_date  DATE NOT NULL,
    end_date    DATE NOT NULL,
    status      TEXT NOT NULL DEFAULT 'PLANNED' CHECK (status IN ('PLANNED', 'ACTIVE', 'CLOSED')),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID,
    updated_by  UUID,
    archived_at TIMESTAMPTZ,
    version     BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_academic_years_date_order CHECK (start_date < end_date)
);

CREATE UNIQUE INDEX uq_academic_years_label ON academic_years (label) WHERE archived_at IS NULL;
-- BR-AS-001: exactly one ACTIVE academic year at a time.
CREATE UNIQUE INDEX uq_academic_years_one_active ON academic_years (status) WHERE status = 'ACTIVE';

CREATE TABLE terms (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    academic_year_id      UUID NOT NULL REFERENCES academic_years (id),
    term_number           INTEGER NOT NULL CHECK (term_number IN (1, 2, 3)),
    official_start_date   DATE NOT NULL,
    official_end_date     DATE NOT NULL,
    expected_school_days  INTEGER NOT NULL,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by            UUID,
    updated_by            UUID,
    archived_at           TIMESTAMPTZ,
    version               BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_terms_date_order CHECK (official_start_date < official_end_date)
);

-- BR-AS-001: exactly three terms per year.
CREATE UNIQUE INDEX uq_terms_academic_year_id_term_number
    ON terms (academic_year_id, term_number) WHERE archived_at IS NULL;

-- BR-AS-003: per-class-level override of a term's dates (JHS 3 shortening).
CREATE TABLE term_calendar_variants (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    term_id             UUID NOT NULL REFERENCES terms (id),
    class_level_id      UUID NOT NULL REFERENCES class_levels (id),
    override_start_date DATE NOT NULL,
    override_end_date   DATE NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by          UUID,
    updated_by          UUID,
    archived_at         TIMESTAMPTZ,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_term_calendar_variants_date_order CHECK (override_start_date < override_end_date)
);

CREATE UNIQUE INDEX uq_term_calendar_variants_term_id_class_level_id
    ON term_calendar_variants (term_id, class_level_id) WHERE archived_at IS NULL;

-- Holiday/closure calendar underlying the school-day computation (FR-ACAD-02).
CREATE TABLE school_day_exceptions (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    exception_date DATE NOT NULL,
    exception_type TEXT NOT NULL CHECK (exception_type IN ('HOLIDAY', 'CLOSURE')),
    reason         TEXT,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by     UUID,
    updated_by     UUID,
    archived_at    TIMESTAMPTZ,
    version        BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uq_school_day_exceptions_date ON school_day_exceptions (exception_date) WHERE archived_at IS NULL;

-- BR-AS-004: one Class Level, streams are distinct classes (Primary 3A != 3B).
CREATE TABLE classes (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_level_id UUID NOT NULL REFERENCES class_levels (id),
    stream         TEXT NOT NULL,
    capacity       INTEGER NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by     UUID,
    updated_by     UUID,
    archived_at    TIMESTAMPTZ,
    version        BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uq_classes_class_level_id_stream ON classes (class_level_id, stream) WHERE archived_at IS NULL;

-- BR-AS-005/A-01: one class teacher per class per year, and a teacher class-teaches at most one
-- class per year. teacher_staff_id is an opaque reference (no FK) — people/Staff (WP-3) doesn't
-- exist yet.
CREATE TABLE class_teacher_assignments (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_id         UUID NOT NULL REFERENCES classes (id),
    academic_year_id UUID NOT NULL REFERENCES academic_years (id),
    teacher_staff_id UUID NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by       UUID,
    updated_by       UUID,
    archived_at      TIMESTAMPTZ,
    version          BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uq_class_teacher_assignments_class_id_academic_year_id
    ON class_teacher_assignments (class_id, academic_year_id) WHERE archived_at IS NULL;
CREATE UNIQUE INDEX uq_class_teacher_assignments_teacher_staff_id_academic_year_id
    ON class_teacher_assignments (teacher_staff_id, academic_year_id) WHERE archived_at IS NULL;

-- BR-AS-006: subject offered per (Class, Year), exactly one assigned teacher at a time (teacher
-- reassignment updates this row rather than versioning it — unlike results, this isn't history
-- that must be preserved). teacher_staff_id nullable: an offering can exist before a teacher is
-- assigned.
CREATE TABLE class_subject_offerings (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_id         UUID NOT NULL REFERENCES classes (id),
    subject_id       UUID NOT NULL REFERENCES subjects (id),
    academic_year_id UUID NOT NULL REFERENCES academic_years (id),
    teacher_staff_id UUID,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by       UUID,
    updated_by       UUID,
    archived_at      TIMESTAMPTZ,
    version          BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uq_class_subject_offerings_class_id_subject_id_academic_year_id
    ON class_subject_offerings (class_id, subject_id, academic_year_id) WHERE archived_at IS NULL;
