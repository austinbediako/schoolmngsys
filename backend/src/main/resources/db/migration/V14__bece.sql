-- WP-10 BECE module schema (docs/09 §1, docs/14 §4, BR-BE-001..003)

CREATE TABLE bece_registrations (
    id UUID PRIMARY KEY,
    enrollment_id UUID NOT NULL REFERENCES enrollments(id),
    student_id UUID NOT NULL,
    index_number VARCHAR(20) NOT NULL,
    snapshot_first_name VARCHAR(100) NOT NULL,
    snapshot_last_name VARCHAR(100) NOT NULL,
    snapshot_dob DATE NOT NULL,
    registered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by UUID,
    archived_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uq_bece_registrations_index_number
    ON bece_registrations(index_number) WHERE archived_at IS NULL;

CREATE UNIQUE INDEX uq_bece_registrations_enrollment_id
    ON bece_registrations(enrollment_id) WHERE archived_at IS NULL;

CREATE TABLE bece_results (
    id UUID PRIMARY KEY,
    bece_registration_id UUID NOT NULL REFERENCES bece_registrations(id),
    subject_id UUID NOT NULL REFERENCES subjects(id),
    grade INTEGER NOT NULL CHECK (grade BETWEEN 1 AND 9),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by UUID,
    archived_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uq_bece_results_registration_subject
    ON bece_results(bece_registration_id, subject_id) WHERE archived_at IS NULL;
