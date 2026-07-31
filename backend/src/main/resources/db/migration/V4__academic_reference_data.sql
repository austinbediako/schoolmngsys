-- WP-2 academics reference data (docs/14 §4). Code-managed, not user-editable CRUD (docs/02 §5):
-- the GES/NaCCA ladder and subject list are seeded here and extended only by later migration.

CREATE TABLE departments (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        TEXT NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID,
    updated_by  UUID,
    archived_at TIMESTAMPTZ,
    version     BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uq_departments_name ON departments (name) WHERE archived_at IS NULL;

-- Fixed ordered ladder N1 -> N2 -> KG1 -> KG2 -> B1..B9 (BR-AS-002); "sequence" is the ordering
-- column promotion logic (WP-9) walks one rung at a time. basic_alias is the GES Basic-N display
-- name for B1-B9 (Basic 1-6 = Primary 1-6, Basic 7-9 = JHS 1-3); null for Nursery/KG.
CREATE TABLE class_levels (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code          TEXT NOT NULL,
    canonical_name TEXT NOT NULL,
    basic_alias   TEXT,
    sequence      INTEGER NOT NULL,
    department_id UUID NOT NULL REFERENCES departments (id),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by    UUID,
    updated_by    UUID,
    archived_at   TIMESTAMPTZ,
    version       BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uq_class_levels_code ON class_levels (code) WHERE archived_at IS NULL;
CREATE UNIQUE INDEX uq_class_levels_sequence ON class_levels (sequence) WHERE archived_at IS NULL;

-- NaCCA subject list (docs/glossary.md), applicability bounded by class_levels.sequence range.
CREATE TABLE subjects (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                TEXT NOT NULL,
    code                TEXT NOT NULL,
    min_level_sequence  INTEGER NOT NULL,
    max_level_sequence  INTEGER NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by          UUID,
    updated_by          UUID,
    archived_at         TIMESTAMPTZ,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_subjects_level_range CHECK (min_level_sequence <= max_level_sequence)
);

CREATE UNIQUE INDEX uq_subjects_code ON subjects (code) WHERE archived_at IS NULL;

INSERT INTO departments (name, description) VALUES
    ('NURSERY', 'Nursery 1-2'),
    ('KG', 'Kindergarten 1-2'),
    ('PRIMARY', 'Basic 1-6 (Primary 1-6)'),
    ('JHS', 'Basic 7-9 (JHS 1-3)');

INSERT INTO class_levels (code, canonical_name, basic_alias, sequence, department_id)
SELECT v.code, v.canonical_name, v.basic_alias, v.sequence, d.id
FROM (VALUES
    ('N1', 'Nursery 1', NULL, 1, 'NURSERY'),
    ('N2', 'Nursery 2', NULL, 2, 'NURSERY'),
    ('KG1', 'KG 1', NULL, 3, 'KG'),
    ('KG2', 'KG 2', NULL, 4, 'KG'),
    ('B1', 'Primary 1', 'Basic 1', 5, 'PRIMARY'),
    ('B2', 'Primary 2', 'Basic 2', 6, 'PRIMARY'),
    ('B3', 'Primary 3', 'Basic 3', 7, 'PRIMARY'),
    ('B4', 'Primary 4', 'Basic 4', 8, 'PRIMARY'),
    ('B5', 'Primary 5', 'Basic 5', 9, 'PRIMARY'),
    ('B6', 'Primary 6', 'Basic 6', 10, 'PRIMARY'),
    ('B7', 'JHS 1', 'Basic 7', 11, 'JHS'),
    ('B8', 'JHS 2', 'Basic 8', 12, 'JHS'),
    ('B9', 'JHS 3', 'Basic 9', 13, 'JHS')
) AS v(code, canonical_name, basic_alias, sequence, department_name)
JOIN departments d ON d.name = v.department_name;

-- Seed applicability ranges are starter defaults (refine via later migration if curriculum
-- specifics differ) — never hardcoded business rules, just editable reference data.
INSERT INTO subjects (name, code, min_level_sequence, max_level_sequence) VALUES
    ('English Language', 'ENG', 1, 13),
    ('Mathematics', 'MATH', 1, 13),
    ('Our World Our People', 'OWOP', 5, 7),
    ('Integrated Science', 'SCI', 8, 13),
    ('Religious and Moral Education', 'RME', 1, 13),
    ('Ghanaian Language', 'GHL', 1, 13),
    ('French', 'FRE', 11, 13),
    ('Computing', 'ICT', 5, 13),
    ('Career Technology', 'CTECH', 11, 13),
    ('Creative Arts and Design', 'CAD', 1, 10),
    ('Physical Education', 'PE', 1, 13);
