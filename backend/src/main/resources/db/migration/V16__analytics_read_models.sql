-- Migration V16: Analytics read models for Head Dashboard (FR-DASH-01, WP-10)

CREATE VIEW v_enrollment_analytics AS
SELECT
    e.academic_year_id,
    cl.id AS class_level_id,
    cl.canonical_name AS class_level_name,
    cl.sequence AS ladder_sequence,
    s.gender,
    COUNT(e.id) AS enrollment_count
FROM enrollments e
JOIN students s ON e.student_id = s.id
JOIN classes c ON e.class_id = c.id
JOIN class_levels cl ON c.class_level_id = cl.id
WHERE e.archived_at IS NULL AND e.status = 'ACTIVE'
GROUP BY e.academic_year_id, cl.id, cl.canonical_name, cl.sequence, s.gender;

CREATE VIEW v_attendance_analytics AS
SELECT
    e.academic_year_id,
    ar.status,
    COUNT(ar.id) AS record_count
FROM attendance_records ar
JOIN enrollments e ON ar.enrollment_id = e.id
WHERE ar.archived_at IS NULL
GROUP BY e.academic_year_id, ar.status;

CREATE VIEW v_finance_analytics AS
SELECT
    e.academic_year_id,
    COALESCE(SUM(il.amount), 0) AS total_invoiced,
    COALESCE((
        SELECT SUM(p.amount)
        FROM payments p
        JOIN enrollments pe ON p.enrollment_id = pe.id
        WHERE pe.academic_year_id = e.academic_year_id AND p.archived_at IS NULL
    ), 0) AS total_collected
FROM enrollments e
LEFT JOIN invoices i ON i.enrollment_id = e.id AND i.archived_at IS NULL AND i.status != 'CANCELLED'
LEFT JOIN invoice_lines il ON il.invoice_id = i.id AND il.archived_at IS NULL
GROUP BY e.academic_year_id;
