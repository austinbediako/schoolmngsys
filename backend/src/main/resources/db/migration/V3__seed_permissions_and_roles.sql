-- Permission catalog (docs/14 §6, definitive list — traces 1:1 to docs/03 §3) and role bundles
-- (docs/03 §2). Scope (own/department/all) is never encoded here — it is applied per role in the
-- service layer (docs/11 §3); this migration only grants the permission strings themselves.
-- Extend by later migration only — never edit rows from this file in place (docs/03 §4).

INSERT INTO permissions (name, description) VALUES
    ('ACCOUNT_VIEW', 'View user accounts'),
    ('ACCOUNT_CREATE', 'Provision a user account'),
    ('ACCOUNT_UPDATE', 'Update a user account'),
    ('ACCOUNT_DEACTIVATE', 'Deactivate a user account'),
    ('ROLE_ASSIGN', 'Assign or revoke a role on an account'),
    ('ACADEMIC_YEAR_VIEW', 'View academic years/terms'),
    ('ACADEMIC_YEAR_CREATE', 'Create an academic year'),
    ('ACADEMIC_YEAR_UPDATE', 'Update an academic year'),
    ('ACADEMIC_YEAR_CLOSE', 'Close an academic year'),
    ('CLASS_VIEW', 'View classes'),
    ('CLASS_CREATE', 'Create a class'),
    ('CLASS_UPDATE', 'Update a class'),
    ('SUBJECT_VIEW', 'View subjects'),
    ('SUBJECT_OFFERING_MANAGE', 'Manage class-subject offerings'),
    ('TEACHER_ASSIGNMENT_MANAGE', 'Manage teacher-subject/class assignments'),
    ('CALENDAR_MANAGE', 'Manage the school-day calendar'),
    ('STUDENT_VIEW', 'View student records'),
    ('STUDENT_VIEW_IDENTITY_ONLY', 'View identity-only student fields'),
    ('STUDENT_CREATE', 'Create a student record'),
    ('STUDENT_UPDATE', 'Update a student record'),
    ('STUDENT_ARCHIVE', 'Archive a student record'),
    ('GUARDIAN_VIEW', 'View guardian records'),
    ('GUARDIAN_CREATE', 'Create a guardian record'),
    ('GUARDIAN_UPDATE', 'Update a guardian record'),
    ('GUARDIAN_LINK_MANAGE', 'Manage student-guardian links'),
    ('STUDENT_DOCUMENT_VIEW', 'View a student document'),
    ('STUDENT_DOCUMENT_UPLOAD', 'Upload a student document'),
    ('ENROLLMENT_VIEW', 'View enrollments'),
    ('ENROLLMENT_CREATE', 'Create an enrollment'),
    ('ENROLLMENT_END', 'End an enrollment (transfer/withdrawal)'),
    ('ROSTER_VIEW', 'View a class roster'),
    ('ATTENDANCE_VIEW', 'View attendance records'),
    ('ATTENDANCE_MARK', 'Mark daily attendance'),
    ('ATTENDANCE_CORRECT', 'Correct a past attendance record'),
    ('ASSESSMENT_COMPONENT_MANAGE', 'Manage assessment components'),
    ('SCORE_ENTER', 'Enter scores'),
    ('RESULT_VIEW', 'View term results'),
    ('RESULT_SUBMIT', 'Submit results for approval'),
    ('RESULT_APPROVE', 'Approve submitted results'),
    ('RESULT_PUBLISH', 'Publish approved results'),
    ('RESULT_REVISE', 'Revise a published result'),
    ('REPORT_CARD_VIEW', 'View report card data'),
    ('GRADE_SCALE_MANAGE', 'Manage grade scales'),
    ('PROMOTION_PROPOSE', 'Propose a promotion decision'),
    ('PROMOTION_APPROVE', 'Approve a promotion decision'),
    ('PROMOTION_RUN_EXECUTE', 'Execute the year-end promotion run'),
    ('FEE_SCHEDULE_VIEW', 'View fee schedules'),
    ('FEE_SCHEDULE_MANAGE', 'Manage fee schedules'),
    ('FEE_SCHEDULE_APPROVE', 'Approve a fee schedule'),
    ('INVOICE_VIEW', 'View invoices'),
    ('BILLING_RUN_EXECUTE', 'Execute a billing run'),
    ('PAYMENT_VIEW', 'View payments'),
    ('PAYMENT_RECORD', 'Record a payment'),
    ('PAYMENT_REVERSE', 'Reverse a payment'),
    ('ADJUSTMENT_PROPOSE', 'Propose a fee adjustment'),
    ('ADJUSTMENT_APPROVE', 'Approve a fee adjustment'),
    ('FINANCE_REPORT_VIEW', 'View financial reports'),
    ('STAFF_VIEW', 'View staff records'),
    ('STAFF_CREATE', 'Create a staff record'),
    ('STAFF_UPDATE', 'Update a staff record'),
    ('STAFF_END_EMPLOYMENT', 'End a staff member''s employment'),
    ('ANNOUNCEMENT_CREATE', 'Create an announcement'),
    ('ANNOUNCEMENT_VIEW', 'View announcements'),
    ('NOTIFICATION_VIEW_OWN', 'View own notification inbox'),
    ('MESSAGE_TEMPLATE_MANAGE', 'Manage message templates'),
    ('DASHBOARD_VIEW_SCHOOL', 'View school-wide dashboard'),
    ('DASHBOARD_VIEW_DEPARTMENT', 'View department dashboard'),
    ('DASHBOARD_VIEW_OWN', 'View own-scope dashboard'),
    ('DASHBOARD_VIEW_FINANCE', 'View finance dashboard'),
    ('AUDIT_VIEW', 'View the audit log'),
    ('EXPORT_EXECUTE', 'Execute a bulk data export');

INSERT INTO roles (name, description) VALUES
    ('SYSTEM_ADMIN', 'Full technical administration: accounts, roles, configuration, integrations'),
    ('HEAD_OF_SCHOOL', 'Final approvals (results, promotions, admissions, adjustments); school-wide read access'),
    ('SCHOOL_ADMIN', 'Front-office administration: student records, admissions, class setup, announcements'),
    ('HOD', 'Department scope: approves results, oversees teachers and classes in the department'),
    ('TEACHER', 'Marks attendance and enters scores for own classes/subjects'),
    ('ACCOUNTANT', 'Fee schedules, invoicing, payments, financial reports'),
    ('LIBRARIAN', 'Library catalog and loans'),
    ('NURSE', 'Health profiles and medical visits'),
    ('GUARDIAN', 'Read access to own wards; pays fees; updates own contact info'),
    ('STUDENT', 'Read own results/timetable/library loans (JHS only)');

-- Role -> permission grants, derived from the permission matrix (docs/03 §3). Any non-"—" cell in
-- the matrix grants the corresponding permission string here; the ✓/R/Dept/Own scope distinction
-- is enforced later in the service layer, not encoded in this table.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE (r.name, p.name) IN (
    -- User accounts & roles
    ('SYSTEM_ADMIN', 'ACCOUNT_VIEW'), ('SYSTEM_ADMIN', 'ACCOUNT_CREATE'), ('SYSTEM_ADMIN', 'ACCOUNT_UPDATE'),
    ('SYSTEM_ADMIN', 'ACCOUNT_DEACTIVATE'), ('SYSTEM_ADMIN', 'ROLE_ASSIGN'),
    ('HEAD_OF_SCHOOL', 'ACCOUNT_VIEW'),

    -- Academic year/term setup + calendar
    ('SYSTEM_ADMIN', 'ACADEMIC_YEAR_VIEW'), ('SYSTEM_ADMIN', 'ACADEMIC_YEAR_CREATE'),
    ('SYSTEM_ADMIN', 'ACADEMIC_YEAR_UPDATE'), ('SYSTEM_ADMIN', 'ACADEMIC_YEAR_CLOSE'), ('SYSTEM_ADMIN', 'CALENDAR_MANAGE'),
    ('HEAD_OF_SCHOOL', 'ACADEMIC_YEAR_VIEW'), ('HEAD_OF_SCHOOL', 'ACADEMIC_YEAR_CREATE'),
    ('HEAD_OF_SCHOOL', 'ACADEMIC_YEAR_UPDATE'), ('HEAD_OF_SCHOOL', 'ACADEMIC_YEAR_CLOSE'), ('HEAD_OF_SCHOOL', 'CALENDAR_MANAGE'),
    ('SCHOOL_ADMIN', 'ACADEMIC_YEAR_VIEW'), ('HOD', 'ACADEMIC_YEAR_VIEW'), ('TEACHER', 'ACADEMIC_YEAR_VIEW'),
    ('ACCOUNTANT', 'ACADEMIC_YEAR_VIEW'), ('LIBRARIAN', 'ACADEMIC_YEAR_VIEW'), ('NURSE', 'ACADEMIC_YEAR_VIEW'),

    -- Student records (bio, docs)
    ('HEAD_OF_SCHOOL', 'STUDENT_VIEW'), ('HEAD_OF_SCHOOL', 'STUDENT_CREATE'), ('HEAD_OF_SCHOOL', 'STUDENT_UPDATE'),
    ('HEAD_OF_SCHOOL', 'STUDENT_ARCHIVE'), ('HEAD_OF_SCHOOL', 'STUDENT_DOCUMENT_VIEW'), ('HEAD_OF_SCHOOL', 'STUDENT_DOCUMENT_UPLOAD'),
    ('SCHOOL_ADMIN', 'STUDENT_VIEW'), ('SCHOOL_ADMIN', 'STUDENT_CREATE'), ('SCHOOL_ADMIN', 'STUDENT_UPDATE'),
    ('SCHOOL_ADMIN', 'STUDENT_ARCHIVE'), ('SCHOOL_ADMIN', 'STUDENT_DOCUMENT_VIEW'), ('SCHOOL_ADMIN', 'STUDENT_DOCUMENT_UPLOAD'),
    ('HOD', 'STUDENT_VIEW'), ('HOD', 'STUDENT_DOCUMENT_VIEW'),
    ('TEACHER', 'STUDENT_VIEW'), ('TEACHER', 'STUDENT_DOCUMENT_VIEW'),
    ('ACCOUNTANT', 'STUDENT_VIEW'),
    ('LIBRARIAN', 'STUDENT_VIEW_IDENTITY_ONLY'), ('NURSE', 'STUDENT_VIEW_IDENTITY_ONLY'),
    ('GUARDIAN', 'STUDENT_VIEW'), ('STUDENT', 'STUDENT_VIEW'), ('SYSTEM_ADMIN', 'STUDENT_VIEW'),

    -- Guardian records & links
    ('HEAD_OF_SCHOOL', 'GUARDIAN_VIEW'), ('HEAD_OF_SCHOOL', 'GUARDIAN_CREATE'), ('HEAD_OF_SCHOOL', 'GUARDIAN_UPDATE'),
    ('HEAD_OF_SCHOOL', 'GUARDIAN_LINK_MANAGE'),
    ('SCHOOL_ADMIN', 'GUARDIAN_VIEW'), ('SCHOOL_ADMIN', 'GUARDIAN_CREATE'), ('SCHOOL_ADMIN', 'GUARDIAN_UPDATE'),
    ('SCHOOL_ADMIN', 'GUARDIAN_LINK_MANAGE'),
    ('HOD', 'GUARDIAN_VIEW'), ('TEACHER', 'GUARDIAN_VIEW'), ('ACCOUNTANT', 'GUARDIAN_VIEW'),
    ('NURSE', 'GUARDIAN_VIEW'), ('SYSTEM_ADMIN', 'GUARDIAN_VIEW'),
    ('GUARDIAN', 'GUARDIAN_UPDATE'),

    -- Class & subject setup
    ('HEAD_OF_SCHOOL', 'CLASS_VIEW'), ('HEAD_OF_SCHOOL', 'CLASS_CREATE'), ('HEAD_OF_SCHOOL', 'CLASS_UPDATE'),
    ('HEAD_OF_SCHOOL', 'SUBJECT_VIEW'), ('HEAD_OF_SCHOOL', 'SUBJECT_OFFERING_MANAGE'),
    ('SCHOOL_ADMIN', 'CLASS_VIEW'), ('SCHOOL_ADMIN', 'CLASS_CREATE'), ('SCHOOL_ADMIN', 'CLASS_UPDATE'),
    ('SCHOOL_ADMIN', 'SUBJECT_VIEW'), ('SCHOOL_ADMIN', 'SUBJECT_OFFERING_MANAGE'),
    ('HOD', 'CLASS_VIEW'), ('HOD', 'SUBJECT_VIEW'), ('TEACHER', 'CLASS_VIEW'), ('TEACHER', 'SUBJECT_VIEW'),
    ('SYSTEM_ADMIN', 'CLASS_VIEW'), ('SYSTEM_ADMIN', 'SUBJECT_VIEW'),

    -- Teacher-subject assignment
    ('HEAD_OF_SCHOOL', 'TEACHER_ASSIGNMENT_MANAGE'), ('SCHOOL_ADMIN', 'TEACHER_ASSIGNMENT_MANAGE'),
    ('HOD', 'TEACHER_ASSIGNMENT_MANAGE'),

    -- Attendance
    ('TEACHER', 'ATTENDANCE_MARK'),
    ('SYSTEM_ADMIN', 'ATTENDANCE_VIEW'), ('HEAD_OF_SCHOOL', 'ATTENDANCE_VIEW'), ('SCHOOL_ADMIN', 'ATTENDANCE_VIEW'),
    ('HOD', 'ATTENDANCE_VIEW'), ('TEACHER', 'ATTENDANCE_VIEW'), ('NURSE', 'ATTENDANCE_VIEW'),
    ('GUARDIAN', 'ATTENDANCE_VIEW'), ('STUDENT', 'ATTENDANCE_VIEW'),
    ('HEAD_OF_SCHOOL', 'ATTENDANCE_CORRECT'), ('SCHOOL_ADMIN', 'ATTENDANCE_CORRECT'), ('HOD', 'ATTENDANCE_CORRECT'),

    -- Assessment
    ('TEACHER', 'SCORE_ENTER'),
    ('HEAD_OF_SCHOOL', 'RESULT_PUBLISH'), ('HOD', 'RESULT_APPROVE'),
    ('SYSTEM_ADMIN', 'RESULT_VIEW'), ('HEAD_OF_SCHOOL', 'RESULT_VIEW'), ('SCHOOL_ADMIN', 'RESULT_VIEW'),
    ('HOD', 'RESULT_VIEW'), ('TEACHER', 'RESULT_VIEW'), ('GUARDIAN', 'RESULT_VIEW'), ('STUDENT', 'RESULT_VIEW'),
    ('SYSTEM_ADMIN', 'REPORT_CARD_VIEW'), ('HEAD_OF_SCHOOL', 'REPORT_CARD_VIEW'), ('SCHOOL_ADMIN', 'REPORT_CARD_VIEW'),
    ('HOD', 'REPORT_CARD_VIEW'), ('TEACHER', 'REPORT_CARD_VIEW'), ('GUARDIAN', 'REPORT_CARD_VIEW'), ('STUDENT', 'REPORT_CARD_VIEW'),

    -- Promotion decisions
    ('HEAD_OF_SCHOOL', 'PROMOTION_APPROVE'), ('HEAD_OF_SCHOOL', 'PROMOTION_RUN_EXECUTE'),
    ('SCHOOL_ADMIN', 'PROMOTION_PROPOSE'), ('HOD', 'PROMOTION_PROPOSE'),

    -- Fee schedules & adjustments
    ('HEAD_OF_SCHOOL', 'FEE_SCHEDULE_APPROVE'), ('HEAD_OF_SCHOOL', 'ADJUSTMENT_APPROVE'),
    ('ACCOUNTANT', 'FEE_SCHEDULE_VIEW'), ('ACCOUNTANT', 'FEE_SCHEDULE_MANAGE'), ('ACCOUNTANT', 'ADJUSTMENT_PROPOSE'),

    -- Invoicing & payments
    ('HEAD_OF_SCHOOL', 'INVOICE_VIEW'), ('HEAD_OF_SCHOOL', 'PAYMENT_VIEW'),
    ('ACCOUNTANT', 'INVOICE_VIEW'), ('ACCOUNTANT', 'BILLING_RUN_EXECUTE'), ('ACCOUNTANT', 'PAYMENT_VIEW'),
    ('ACCOUNTANT', 'PAYMENT_RECORD'), ('ACCOUNTANT', 'PAYMENT_REVERSE'),
    ('GUARDIAN', 'INVOICE_VIEW'), ('GUARDIAN', 'PAYMENT_VIEW'),

    -- Financial reports
    ('HEAD_OF_SCHOOL', 'FINANCE_REPORT_VIEW'), ('ACCOUNTANT', 'FINANCE_REPORT_VIEW'),

    -- Staff records
    ('HEAD_OF_SCHOOL', 'STAFF_VIEW'), ('HEAD_OF_SCHOOL', 'STAFF_CREATE'), ('HEAD_OF_SCHOOL', 'STAFF_UPDATE'),
    ('HEAD_OF_SCHOOL', 'STAFF_END_EMPLOYMENT'),
    ('SCHOOL_ADMIN', 'STAFF_VIEW'), ('SCHOOL_ADMIN', 'STAFF_CREATE'), ('SCHOOL_ADMIN', 'STAFF_UPDATE'),
    ('SCHOOL_ADMIN', 'STAFF_END_EMPLOYMENT'),
    ('HOD', 'STAFF_VIEW'), ('TEACHER', 'STAFF_VIEW'), ('ACCOUNTANT', 'STAFF_VIEW'),
    ('LIBRARIAN', 'STAFF_VIEW'), ('NURSE', 'STAFF_VIEW'), ('SYSTEM_ADMIN', 'STAFF_VIEW'),

    -- Announcements
    ('SYSTEM_ADMIN', 'ANNOUNCEMENT_CREATE'), ('SYSTEM_ADMIN', 'ANNOUNCEMENT_VIEW'),
    ('HEAD_OF_SCHOOL', 'ANNOUNCEMENT_CREATE'), ('HEAD_OF_SCHOOL', 'ANNOUNCEMENT_VIEW'),
    ('SCHOOL_ADMIN', 'ANNOUNCEMENT_CREATE'), ('SCHOOL_ADMIN', 'ANNOUNCEMENT_VIEW'),
    ('HOD', 'ANNOUNCEMENT_CREATE'), ('HOD', 'ANNOUNCEMENT_VIEW'),
    ('TEACHER', 'ANNOUNCEMENT_CREATE'), ('TEACHER', 'ANNOUNCEMENT_VIEW'),
    ('GUARDIAN', 'ANNOUNCEMENT_VIEW'), ('GUARDIAN', 'NOTIFICATION_VIEW_OWN'),
    ('STUDENT', 'ANNOUNCEMENT_VIEW'), ('STUDENT', 'NOTIFICATION_VIEW_OWN'),

    -- Dashboards/analytics
    ('SYSTEM_ADMIN', 'DASHBOARD_VIEW_SCHOOL'), ('HEAD_OF_SCHOOL', 'DASHBOARD_VIEW_SCHOOL'),
    ('SCHOOL_ADMIN', 'DASHBOARD_VIEW_SCHOOL'), ('HOD', 'DASHBOARD_VIEW_DEPARTMENT'),
    ('TEACHER', 'DASHBOARD_VIEW_OWN'), ('ACCOUNTANT', 'DASHBOARD_VIEW_FINANCE'), ('GUARDIAN', 'DASHBOARD_VIEW_OWN'),

    -- Audit log
    ('SYSTEM_ADMIN', 'AUDIT_VIEW'), ('HEAD_OF_SCHOOL', 'AUDIT_VIEW'),

    -- Data export (not its own matrix row; granted to the two broadest-scope roles per docs/11 §1)
    ('SYSTEM_ADMIN', 'EXPORT_EXECUTE'), ('HEAD_OF_SCHOOL', 'EXPORT_EXECUTE')
);
