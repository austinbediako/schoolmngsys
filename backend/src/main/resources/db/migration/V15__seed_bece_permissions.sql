-- The BECE controller (progression module, V14) was built referencing BECE_REGISTER and
-- BECE_SCORE_ENTER, but neither permission was ever added to the V3 catalog — every BECE
-- endpoint was therefore permanently unreachable (no role could ever be granted them). This
-- migration seeds them per docs/03 §3's "BECE registration" row (HEAD approve, SCHOOL_ADMIN
-- process); V3 itself is immutable once merged, so this extends the catalog rather than editing it.

INSERT INTO permissions (name, description) VALUES
    ('BECE_REGISTER', 'Register a BECE candidate and view registrations'),
    ('BECE_SCORE_ENTER', 'Import BECE results (WAEC stanines) and view results');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE (r.name, p.name) IN (
    ('HEAD_OF_SCHOOL', 'BECE_REGISTER'), ('HEAD_OF_SCHOOL', 'BECE_SCORE_ENTER'),
    ('SCHOOL_ADMIN', 'BECE_REGISTER'), ('SCHOOL_ADMIN', 'BECE_SCORE_ENTER')
);
