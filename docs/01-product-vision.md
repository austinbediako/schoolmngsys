# 01 — Product & System Vision

> Source: [UBS-LMIS Concept Document](../UBS-LMIS_Concept_Document.md). Terminology: [Glossary](glossary.md).

## 1. Product Vision

**For** University Basic School, Legon — its administrators, teachers, students, guardians, and support staff —
**who** currently run admissions, attendance, assessment, fees, and communication on paper and disconnected spreadsheets,
**UBS-LMIS** is an integrated school management information system
**that** provides one accurate, secure, always-current record of every student and every school process from Nursery 1 to JHS 3,
**unlike** generic school software, it is modeled on the Ghanaian basic-education reality: GES three-term calendar, NaCCA subjects, SBA + end-of-term assessment, BECE, guardian-centric communication over SMS, and mobile-money fee payment.

## 2. System Vision

One **modular-monolith backend** (Spring Boot, PostgreSQL) exposing a versioned REST API; a web frontend follows later as a separate application. The backend is the system of record — every rule that matters is enforced server-side, never only in a UI.

### What the system IS
- The **single source of truth** for student, academic, financial, and operational records.
- A **workflow engine** for the school's recurring cycles: admission → enrollment → three terms of attendance/assessment/fees → promotion → (eventually) BECE and graduation.
- A **communication hub** pushing report cards, fee notices, and announcements to guardians.

### What the system is NOT (out of scope — documented deliberately)
| Explicitly out of scope | Reasoning |
|---|---|
| University of Ghana payroll/HR integration | Staff salaries are processed by the University; LMIS keeps staff records only. Revisit in [Roadmap](13-roadmap.md). |
| Multi-school / multi-tenant operation | One school. See ADR-010. |
| Transport & hostel management | UBS is a day school; concept document lists neither. |
| Online learning content delivery (full LMS) | Concept lists "Learning Management" — scoped post-MVP to assignments/materials, not a Moodle replacement. See [Gap Analysis](12-gap-analysis.md). |
| WAEC systems integration (direct API) | WAEC offers no public API. BECE data is prepared as exportable files; results are imported manually. |

## 3. Users

Canonical role definitions live in [03 — Roles & Permissions](03-roles-and-permissions.md).

School Administration (Head of School, administrative staff), Heads of Department, Teachers (class + subject), Students (JHS-age; younger students are represented by guardians), Parents/Guardians, Accountant, Librarian, School Nurse, and a technical System Administrator.

## 4. Goals → Measurable Objectives

| Concept-document objective | Engineering interpretation | Success signal |
|---|---|---|
| Digitize student records | Complete student registry with documents, guardians, history | 100% of active students in system with ≥1 linked guardian |
| Automate attendance and results | Daily digital registers; computed, approved, published term results | Report cards generated without manual computation |
| Improve parent communication | SMS/portal notifications tied to real events | Guardians notified on result publication & fee due dates |
| Manage fees | Term billing, receipts, arrears tracking | Accountant reconciles a term without spreadsheets |
| Provide analytics | Dashboards per role | Head sees enrollment, attendance %, fee collection, performance trends |
| Reduce paperwork | Workflows above replace paper equivalents | Paper register/report-card processes retired |

## 5. MVP Boundary

The concept document names the MVP: **Authentication, Student Management, Teacher (Staff) Management, Class Management, Attendance, Results, Fees, Parent Portal (API side)**.

Every functional requirement in [05 — Functional Requirements](05-functional-requirements.md) carries an `MVP` or `POST-MVP` tag. Modules entirely post-MVP: Admissions (formal workflow), BECE, Timetable, Learning Management, Library, Health, Inventory, and advanced analytics. *Rationale:* the MVP list is exactly what a school needs to run one term digitally; everything else layers on top without schema rework because the domain model anticipates it (see [09 — Data Architecture](09-data-architecture.md)).

## 6. Guiding Product Principles

1. **Ghana-first modeling.** GES/NaCCA/WAEC structures are first-class, not configuration afterthoughts.
2. **Guardian-centric access.** For most of the school (Nursery–Primary), the guardian *is* the user; students under JHS never need accounts.
3. **SMS before email.** Notification design assumes feature phones; email and in-app are secondary channels.
4. **History is sacred.** Year-scoped enrollments, immutable receipts, append-only audit. Nothing academic or financial is silently overwritten.
5. **Server-side truth.** All business rules enforced in the backend; the future frontend is a client, not a co-owner of logic.
