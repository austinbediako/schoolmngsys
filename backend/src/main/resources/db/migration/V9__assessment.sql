-- WP-6 assessment (docs/14 §4, BR-AA-001..008, FR-RES-01..07) — the school's trust surface
-- (CLAUDE.md testing philosophy). grade_scales bundles both the per-year A-03 weighting and the
-- A-04 grade bands into one configurable-per-year unit (docs call these two separate concepts,
-- but both are "configurable per Academic Year" and always configured together in practice).

CREATE TABLE grade_scales (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    academic_year_id      UUID NOT NULL REFERENCES academic_years (id),
    sba_weight_percent    NUMERIC(5, 2) NOT NULL,
    exam_weight_percent   NUMERIC(5, 2) NOT NULL,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by            UUID,
    updated_by            UUID,
    archived_at           TIMESTAMPTZ,
    version               BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_grade_scales_weights_sum_100 CHECK (sba_weight_percent + exam_weight_percent = 100)
);

-- BR-AA-001/005: exactly one grade scale (and its weighting) per academic year.
CREATE UNIQUE INDEX uq_grade_scales_academic_year_id ON grade_scales (academic_year_id) WHERE archived_at IS NULL;

CREATE TABLE grade_bands (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    grade_scale_id UUID NOT NULL REFERENCES grade_scales (id),
    min_score     NUMERIC(5, 1) NOT NULL,
    max_score     NUMERIC(5, 1) NOT NULL,
    grade         TEXT NOT NULL,
    description   TEXT NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by    UUID,
    updated_by    UUID,
    archived_at   TIMESTAMPTZ,
    version       BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_grade_bands_score_range CHECK (min_score <= max_score)
);

CREATE INDEX idx_grade_bands_grade_scale_id ON grade_bands (grade_scale_id) WHERE archived_at IS NULL;

-- BR-AA-001/002: (ClassSubjectOffering, Term), category SBA|EXAM, weight *within* its category.
CREATE TABLE assessment_components (
    id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_subject_offering_id UUID NOT NULL REFERENCES class_subject_offerings (id),
    term_id                   UUID NOT NULL REFERENCES terms (id),
    title                     TEXT NOT NULL,
    category                  TEXT NOT NULL CHECK (category IN ('SBA', 'EXAM')),
    max_score                 NUMERIC(6, 2) NOT NULL,
    weight_percent            NUMERIC(5, 2) NOT NULL,
    assessment_date           DATE NOT NULL,
    created_at                TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at                TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by                UUID,
    updated_by                UUID,
    archived_at               TIMESTAMPTZ,
    version                   BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_assessment_components_max_score_positive CHECK (max_score > 0)
);

CREATE INDEX idx_assessment_components_offering_term
    ON assessment_components (class_subject_offering_id, term_id) WHERE archived_at IS NULL;

-- BR-AA-002/007: raw score bounded [0, maxScore]; a student with no resolution (score, exemption,
-- or N/A-with-reason) is a "missing" flag blocking submission — never silently defaulted to zero.
CREATE TABLE scores (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assessment_component_id UUID NOT NULL REFERENCES assessment_components (id),
    enrollment_id           UUID NOT NULL REFERENCES enrollments (id),
    raw_score               NUMERIC(6, 2),
    exempted                BOOLEAN NOT NULL DEFAULT FALSE,
    na_reason               TEXT,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by              UUID,
    updated_by              UUID,
    archived_at             TIMESTAMPTZ,
    version                 BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_scores_raw_score_range CHECK (raw_score IS NULL OR raw_score >= 0),
    CONSTRAINT ck_scores_resolution CHECK (raw_score IS NOT NULL OR exempted OR na_reason IS NOT NULL)
);

CREATE UNIQUE INDEX uq_scores_component_id_enrollment_id
    ON scores (assessment_component_id, enrollment_id) WHERE archived_at IS NULL;

-- BR-AA-003/006: computed snapshot, versioned; a superseded row is archived and points at its
-- replacement — published results are never edited in place (docs/09 §5).
CREATE TABLE term_results (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    enrollment_id         UUID NOT NULL REFERENCES enrollments (id),
    class_subject_offering_id UUID NOT NULL REFERENCES class_subject_offerings (id),
    term_id               UUID NOT NULL REFERENCES terms (id),
    sba_total             NUMERIC(5, 1) NOT NULL,
    exam_total            NUMERIC(5, 1) NOT NULL,
    weighted_total        NUMERIC(5, 1) NOT NULL,
    grade                 TEXT NOT NULL,
    subject_position      INTEGER,
    status                TEXT NOT NULL DEFAULT 'DRAFT'
        CHECK (status IN ('DRAFT', 'SUBMITTED', 'HOD_APPROVED', 'PUBLISHED')),
    result_version        INTEGER NOT NULL DEFAULT 1,
    superseded_by_id      UUID,
    revision_reason       TEXT,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by            UUID,
    updated_by            UUID,
    archived_at           TIMESTAMPTZ,
    version               BIGINT NOT NULL DEFAULT 0
);

-- One current (non-superseded, non-archived) result per (enrollment, offering, term).
CREATE UNIQUE INDEX uq_term_results_enrollment_offering_term_current
    ON term_results (enrollment_id, class_subject_offering_id, term_id) WHERE archived_at IS NULL;
CREATE INDEX idx_term_results_offering_term ON term_results (class_subject_offering_id, term_id) WHERE archived_at IS NULL;

-- Per (Student, Term): classPosition (rank by average across subjects), remarks, publication.
CREATE TABLE report_cards (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    enrollment_id    UUID NOT NULL REFERENCES enrollments (id),
    term_id          UUID NOT NULL REFERENCES terms (id),
    class_position   INTEGER,
    conduct_remark   TEXT,
    interest_remark  TEXT,
    head_remark      TEXT,
    published_at     TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by       UUID,
    updated_by       UUID,
    archived_at      TIMESTAMPTZ,
    version          BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uq_report_cards_enrollment_id_term_id
    ON report_cards (enrollment_id, term_id) WHERE archived_at IS NULL;
