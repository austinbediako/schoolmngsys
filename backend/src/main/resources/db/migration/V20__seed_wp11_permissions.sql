-- WP-11 permission catalog extension (V3 is immutable once merged — see its own header comment).
-- Staff documents mirror the STUDENT_DOCUMENT_* grant pattern but tighter (HR-sensitive personal
-- files, not opened to HOD/TEACHER the way student pastoral documents are). School settings follow
-- the same SYSTEM_ADMIN-vs-HEAD_OF_SCHOOL split docs/03 §2 describes for every other
-- technical-power-vs-institutional-authority capability in this system.

INSERT INTO permissions (name, description) VALUES
    ('STAFF_DOCUMENT_VIEW', 'View a staff document'),
    ('STAFF_DOCUMENT_UPLOAD', 'Upload a staff document'),
    ('SCHOOL_SETTINGS_VIEW', 'View school settings'),
    ('SCHOOL_SETTINGS_MANAGE', 'Manage school settings (name, contact info, notification toggles, logo)');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE (r.name, p.name) IN (
    -- Staff documents
    ('SYSTEM_ADMIN', 'STAFF_DOCUMENT_VIEW'),
    ('HEAD_OF_SCHOOL', 'STAFF_DOCUMENT_VIEW'), ('HEAD_OF_SCHOOL', 'STAFF_DOCUMENT_UPLOAD'),
    ('SCHOOL_ADMIN', 'STAFF_DOCUMENT_VIEW'), ('SCHOOL_ADMIN', 'STAFF_DOCUMENT_UPLOAD'),

    -- School settings
    ('SYSTEM_ADMIN', 'SCHOOL_SETTINGS_VIEW'), ('SYSTEM_ADMIN', 'SCHOOL_SETTINGS_MANAGE'),
    ('HEAD_OF_SCHOOL', 'SCHOOL_SETTINGS_VIEW'), ('HEAD_OF_SCHOOL', 'SCHOOL_SETTINGS_MANAGE'),
    ('SCHOOL_ADMIN', 'SCHOOL_SETTINGS_VIEW'),
    ('HOD', 'SCHOOL_SETTINGS_VIEW'), ('TEACHER', 'SCHOOL_SETTINGS_VIEW'),
    ('ACCOUNTANT', 'SCHOOL_SETTINGS_VIEW'), ('LIBRARIAN', 'SCHOOL_SETTINGS_VIEW'), ('NURSE', 'SCHOOL_SETTINGS_VIEW')
);
