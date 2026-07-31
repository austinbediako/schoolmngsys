# 05 — Functional Requirements

> Format: `FR-<MODULE>-<nn>` · each requirement tagged **MVP** or **POST-MVP** · rules cited as `BR-…` from
> [04 — Business Rules](04-business-rules.md); roles from [03 — Roles & Permissions](03-roles-and-permissions.md).
> User stories are embedded as the "As a…" clause; acceptance criteria (AC) are testable statements.
> The feature matrix at the end summarizes phasing.

## AUTH — Authentication & Access (MVP)

| ID | Requirement | Tag |
|---|---|---|
| FR-AUTH-01 | Users authenticate with username/staff-number/phone + password and receive a short-lived access token + refresh token (BR-SE-005). | MVP |
| FR-AUTH-02 | Role/permission provisioning per the permission matrix; multiple roles per account. | MVP |
| FR-AUTH-03 | Password reset via OTP to registered phone or email; forced change on first login. | MVP |
| FR-AUTH-04 | Account deactivation immediately blocks refresh and (≤ access-token TTL) API access (BR-ST-002). | MVP |
| FR-AUTH-05 | Failed-login throttling and lockout with notification to SYSTEM_ADMIN. | MVP |

**AC (sample):** Given a deactivated account, when its refresh token is presented, then the request is rejected and audited.

## ACAD — Academic Structure (MVP)

| ID | Requirement | Tag |
|---|---|---|
| FR-ACAD-01 | Manage Academic Years and their three Terms with dates and status transitions (BR-AS-001, BR-AS-007). | MVP |
| FR-ACAD-02 | Maintain Term Calendar Variants (JHS 3 early closure, BR-AS-003) and school-day calendars (holidays, closures). | MVP |
| FR-ACAD-03 | Manage Classes (level + stream + capacity) and per-year Class Teacher assignment (BR-AS-004/005). | MVP |
| FR-ACAD-04 | Manage per-year subject offerings per class with teacher assignment (BR-AS-006). | MVP |
| FR-ACAD-05 | As a School Admin, I can view any class's roster for the active year. | MVP |

## STU — Student Information (MVP)

| ID | Requirement | Tag |
|---|---|---|
| FR-STU-01 | CRUD student bio-data with photo and document attachments (birth certificate etc.); soft-delete only (BR-SE-004). | MVP |
| FR-STU-02 | Manage guardian records and student-guardian links with relationship type and contact/billing/custody flags (BR-EN-004). | MVP |
| FR-STU-03 | Enroll a student into a class for a year; view full enrollment history (BR-EN-001/003). | MVP |
| FR-STU-04 | Record exits: transfer-out and withdrawal with reason/date (BR-EN-005); generate a transfer summary document (data export). | MVP |
| FR-STU-05 | Search/filter students by name, number, class, status, guardian phone. | MVP |
| FR-STU-06 | As a Guardian, I can view my wards' profiles and update my own contact details. | MVP |

## ADM — Admissions (POST-MVP; MVP uses direct enrollment via FR-STU-03)

| ID | Requirement | Tag |
|---|---|---|
| FR-ADM-01 | Capture applications with required documents (BR-AD-001) and track status (RECEIVED → ASSESSED → OFFERED → ACCEPTED/DECLINED). | POST-MVP |
| FR-ADM-02 | Record entrance assessment/interview outcomes per applicant. | POST-MVP |
| FR-ADM-03 | Issue offers; on acceptance (+ configurable initial payment, BR-AD-003) auto-create Student + Enrollment + guardian account. | POST-MVP |
| FR-ADM-04 | Enforce age guidance with override trail (BR-AD-002) and class capacity (BR-AD-004). | POST-MVP |

## ATT — Attendance (MVP)

| ID | Requirement | Tag |
|---|---|---|
| FR-ATT-01 | As a Class Teacher, I mark today's register for my class in one screen-worth of API calls (bulk endpoint), statuses per BR-AT-003. | MVP |
| FR-ATT-02 | Reject attendance on non-school days (BR-AT-002); pre-populate roster from active enrollments. | MVP |
| FR-ATT-03 | Corrections after the day require elevated permission + reason, audited (BR-AT-004). | MVP |
| FR-ATT-04 | Attendance summaries per student/class/term for reports and report cards (BR-AT-005). | MVP |
| FR-ATT-05 | Absence alert SMS to guardians same day. | POST-MVP |

## RES — Examination & Results (MVP)

| ID | Requirement | Tag |
|---|---|---|
| FR-RES-01 | Teachers define assessment components per subject/term within configured category weights (BR-AA-001/002). | MVP |
| FR-RES-02 | Bulk score entry per component with validation and missing-score flags (BR-AA-007). | MVP |
| FR-RES-03 | Compute term results (weighted totals, grades per active Grade Scale, subject & class positions) — server-side, idempotent (BR-AA-004/005). | MVP |
| FR-RES-04 | Approval pipeline DRAFT → SUBMITTED → HOD_APPROVED → PUBLISHED with role gates (BR-AA-003). | MVP |
| FR-RES-05 | Generate report-card data (per student per term): results, attendance, remarks, positions (data/API level; PDF rendering is frontend/reporting concern). | MVP |
| FR-RES-06 | Publication triggers guardian notification (BR-CO-003) and parent-portal visibility. | MVP |
| FR-RES-07 | Post-publication corrections as audited revisions (BR-AA-006). | MVP |
| FR-RES-08 | Mock exam series for JHS 3, reported separately (BR-AA-008). | POST-MVP |

## PRO — Promotion & Progression (MVP-adjacent: needed at first year end)

| ID | Requirement | Tag |
|---|---|---|
| FR-PRO-01 | Year-end promotion run: propose PROMOTE for all, allow REPEAT exceptions with justification + approvals (BR-PR-001/002/003). | MVP |
| FR-PRO-02 | Bulk-generate next-year enrollments with stream allocation review (BR-PR-005). | MVP |
| FR-PRO-03 | JHS 3 graduation processing and leaving-record generation (BR-PR-004). | POST-MVP |

## BEC — BECE Management (POST-MVP)

| ID | Requirement | Tag |
|---|---|---|
| FR-BEC-01 | Register JHS 3 candidates with snapshot data and index numbers (BR-BE-001/002). | POST-MVP |
| FR-BEC-02 | Export registration data in WAEC-submission format (file export; no direct API). | POST-MVP |
| FR-BEC-03 | Import BECE results (stanines) idempotently; attach to graduation records (BR-BE-003). | POST-MVP |

## FIN — Finance & Fees (MVP)

| ID | Requirement | Tag |
|---|---|---|
| FR-FIN-01 | Define and approve fee schedules per level/term/year (BR-FI-001). | MVP |
| FR-FIN-02 | Term billing run creates invoices for all active enrollments incl. arrears carry-forward (BR-FI-005). | MVP |
| FR-FIN-03 | Record payments (cash/bank/cheque/MoMo reference) with immutable receipt numbers; allocation per BR-FI-002/003. | MVP |
| FR-FIN-04 | Student-specific adjustments with approval (BR-FI-004). | MVP |
| FR-FIN-05 | Guardian-facing balance & payment history for own wards (BR-FI-006). | MVP |
| FR-FIN-06 | Financial reports: collection by period/class/fee-item, arrears aging, daily cash book. | MVP |
| FR-FIN-07 | Online MoMo payment integration (payment-provider webhook flow). | POST-MVP |

## TT — Timetable (POST-MVP)

| ID | Requirement | Tag |
|---|---|---|
| FR-TT-01 | Define period templates and per-class term timetables with clash validation (BR-TT-001/002). | POST-MVP |
| FR-TT-02 | Teacher and class timetable views; guardian/student read access. | POST-MVP |

## PAR — Parent Portal (API surface) (MVP)

The portal is not a separate module server-side; it is the guardian-scoped slice of STU/ATT/RES/FIN plus notifications. Listed for traceability to the concept document.

| ID | Requirement | Tag |
|---|---|---|
| FR-PAR-01 | Guardian dashboard data: wards, published results, attendance summary, balances, announcements. | MVP |
| FR-PAR-02 | Notification preferences (channel opt-ins per category; SMS default on). | POST-MVP |

## LMS — Learning Management (POST-MVP)

| ID | Requirement | Tag |
|---|---|---|
| FR-LMS-01 | Teachers share materials and set assignments per class/subject; JHS students submit; teachers mark. | POST-MVP |

## LIB — Library (POST-MVP)

| ID | Requirement | Tag |
|---|---|---|
| FR-LIB-01 | Catalog books/copies; lend/return/renew with limits and overdue tracking (BR-LI-001/002). | POST-MVP |

## HLT — Health Records (POST-MVP)

| ID | Requirement | Tag |
|---|---|---|
| FR-HLT-01 | Health profiles (allergies, conditions, immunizations, emergency contacts) with restricted visibility (BR-HE-001). | POST-MVP |
| FR-HLT-02 | Medical visit log with guardian notification flag (BR-HE-002); critical-alert flags on class rosters. | POST-MVP |

## STF — Staff Management (MVP: registry; POST-MVP: leave)

| ID | Requirement | Tag |
|---|---|---|
| FR-STF-01 | Staff registry: bio-data, type, qualifications, assignments history (BR-ST-001). | MVP |
| FR-STF-02 | Employment lifecycle with account deactivation (BR-ST-002). | MVP |
| FR-STF-03 | Leave requests and approvals. | POST-MVP |

## INV — Inventory (POST-MVP)

| ID | Requirement | Tag |
|---|---|---|
| FR-INV-01 | Item registry and stock transactions (BR-IN-001); issue tracking to staff/locations. | POST-MVP |

## COM — Communication (MVP core)

| ID | Requirement | Tag |
|---|---|---|
| FR-COM-01 | Event-driven notifications through persistent outbox with pluggable SMS/email providers (BR-CO-002, ADR-008). | MVP |
| FR-COM-02 | Automatic messages: result publication, invoice issued, payment receipt, credentials delivery (BR-CO-003). | MVP |
| FR-COM-03 | Announcements to school/department/class audiences with delivery log (BR-CO-004). | MVP |

## DASH — Dashboard & Analytics (MVP: basics)

| ID | Requirement | Tag |
|---|---|---|
| FR-DASH-01 | Head dashboard: enrollment counts by level/gender, attendance rate, fee collection vs expected, results distribution per term. | MVP |
| FR-DASH-02 | Role-scoped dashboards (teacher: own class; accountant: finance; HoD: department). | POST-MVP |
| FR-DASH-03 | Longitudinal analytics (multi-year trends, BECE performance history). | POST-MVP |

## AUD — Audit (MVP)

| ID | Requirement | Tag |
|---|---|---|
| FR-AUD-01 | Append-only audit log per BR-SE-002 with query API for SYSTEM_ADMIN/HEAD. | MVP |

## Feature Matrix

Module by phase:

| Module | MVP | Post-MVP additions |
|---|---|---|
| Auth & Access | Full | SSO (future, see Roadmap) |
| Academic Structure | Full | — |
| Student Information | Full | Alumni views |
| Admissions | — (direct enrollment) | Full workflow |
| Attendance | Daily register + reporting | Absence SMS, per-subject JHS option |
| Results | Full pipeline | Mock exams, transcript service |
| Promotion | Year-end run | Graduation/leaving certs |
| BECE | — | Full |
| Finance | Billing, payments, reports | MoMo online payments |
| Timetable | — | Full |
| Parent Portal (API) | Dashboard data | Preferences |
| LMS | — | Assignments/materials |
| Library | — | Full |
| Health | — | Full |
| Staff | Registry + lifecycle | Leave |
| Inventory | — | Full |
| Communication | Outbox + auto messages + announcements | Two-way messaging (Roadmap) |
| Dashboards | Head basics | Role dashboards, trends |
| Audit | Full | — |
