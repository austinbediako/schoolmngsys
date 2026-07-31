-- WP-4 enrollment (docs/14 §4, FR-STU-03/04, BR-EN-001/003/005). The historical spine of the
-- system (docs/02 §5): enrollments are never deleted, only status-transitioned.

CREATE TABLE enrollments (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id       UUID NOT NULL REFERENCES students (id),
    class_id         UUID NOT NULL REFERENCES classes (id),
    academic_year_id UUID NOT NULL REFERENCES academic_years (id),
    roll_number      INTEGER,
    status           TEXT NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'TRANSFERRED', 'WITHDRAWN', 'COMPLETED')),
    exit_reason      TEXT,
    exit_date        DATE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by       UUID,
    updated_by       UUID,
    archived_at      TIMESTAMPTZ,
    version          BIGINT NOT NULL DEFAULT 0
);

-- BR-EN-001: at most one ACTIVE enrollment per student per year.
CREATE UNIQUE INDEX uq_enrollments_student_year_active
    ON enrollments (student_id, academic_year_id) WHERE status = 'ACTIVE';

CREATE INDEX idx_enrollments_class_id_academic_year_id ON enrollments (class_id, academic_year_id);
CREATE INDEX idx_enrollments_student_id ON enrollments (student_id);
