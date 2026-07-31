-- Confirmed A-04 default grade bands (docs/14 §4 planning called this "seed the first academic
-- year's grade scale" — not possible at migration time since no academic year exists yet; see
-- docs/14 §8 note). This is a code-managed TEMPLATE, copied into a real per-year grade_scales +
-- grade_bands set by GradeScaleService.createDefault(academicYearId) (WF-01 step 5: an explicit
-- admin action during annual year setup, not an automatic cascade).
CREATE TABLE default_grade_bands (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    min_score   NUMERIC(5, 1) NOT NULL,
    max_score   NUMERIC(5, 1) NOT NULL,
    grade       TEXT NOT NULL,
    description TEXT NOT NULL
);

INSERT INTO default_grade_bands (min_score, max_score, grade, description) VALUES
    (80.0, 100.0, 'A', 'Excellent'),
    (70.0, 79.9, 'B', 'Very Good'),
    (60.0, 69.9, 'C', 'Good'),
    (50.0, 59.9, 'D', 'Credit'),
    (40.0, 49.9, 'E', 'Pass'),
    (0.0, 39.9, 'F', 'Fail');
