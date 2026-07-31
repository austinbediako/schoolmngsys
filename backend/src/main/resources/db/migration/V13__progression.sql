-- WP-9 Progression & Promotion module schema (docs/09 §1, docs/14 §4)

CREATE TABLE promotion_runs (
    id UUID PRIMARY KEY,
    source_academic_year_id UUID NOT NULL REFERENCES academic_years(id),
    target_academic_year_id UUID NOT NULL REFERENCES academic_years(id),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    executed_at TIMESTAMPTZ,
    executed_by UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by UUID,
    archived_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uq_promotion_runs_source_academic_year_id
    ON promotion_runs(source_academic_year_id) WHERE archived_at IS NULL;

CREATE TABLE promotion_decisions (
    id UUID PRIMARY KEY,
    promotion_run_id UUID NOT NULL REFERENCES promotion_runs(id),
    student_id UUID NOT NULL,
    source_class_id UUID NOT NULL REFERENCES classes(id),
    source_class_level_id UUID NOT NULL REFERENCES class_levels(id),
    decision VARCHAR(20) NOT NULL,
    target_class_level_id UUID REFERENCES class_levels(id),
    target_class_id UUID REFERENCES classes(id),
    justification TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PROPOSED',
    approved_by UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by UUID,
    archived_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uq_promotion_decisions_run_student
    ON promotion_decisions(promotion_run_id, student_id) WHERE archived_at IS NULL;
CREATE INDEX idx_promotion_decisions_run_id ON promotion_decisions(promotion_run_id) WHERE archived_at IS NULL;
