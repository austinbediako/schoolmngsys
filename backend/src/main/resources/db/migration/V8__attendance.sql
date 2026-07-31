-- WP-5 attendance (docs/14 §4, BR-AT-001..005, FR-ATT-01..04). Whole-day model for MVP
-- (per-subject JHS attendance is post-MVP, G-07). Keyed by enrollment, not student directly:
-- one active enrollment per student per year (BR-EN-001) makes enrollment_id the natural anchor.

CREATE TABLE attendance_records (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    enrollment_id      UUID NOT NULL REFERENCES enrollments (id),
    attendance_date    DATE NOT NULL,
    status             TEXT NOT NULL CHECK (status IN ('PRESENT', 'ABSENT', 'LATE', 'EXCUSED')),
    correction_reason  TEXT,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by         UUID,
    updated_by         UUID,
    archived_at        TIMESTAMPTZ,
    version            BIGINT NOT NULL DEFAULT 0
);

-- BR-AT-001: one record per student (enrollment) per school day.
CREATE UNIQUE INDEX uq_attendance_records_enrollment_id_date
    ON attendance_records (enrollment_id, attendance_date) WHERE archived_at IS NULL;
CREATE INDEX idx_attendance_records_enrollment_id ON attendance_records (enrollment_id) WHERE archived_at IS NULL;
