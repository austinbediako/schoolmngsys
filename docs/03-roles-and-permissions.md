# 03 — Roles & Permissions

> Authorization model rationale: ADR-004. Security architecture: [11 — Security & Privacy](11-security-and-privacy.md).
> Terms per [Glossary](glossary.md).

## 1. Model

- **RBAC with fine-grained permissions.** Roles are named bundles of permissions; endpoints check *permissions*, never role names, so new roles can be composed without code change.
- **Scope rules ride on top of permissions.** Having `RESULT_VIEW` does not mean *all* results: teachers see their classes/subjects, guardians see their wards, HoDs see their department. Scope enforcement is a server-side filter, defined per permission in this document, enforced in the service layer.
- **Account ≠ person.** A UserAccount links to exactly one Staff, Guardian, or Student record. Students below JHS get no accounts (Product Principle #2).
- A user may hold **multiple roles** (a Teacher who is also an HoD; a Staff member who is also a Parent).

## 2. Roles

| Role | Person type | Summary |
|---|---|---|
| `SYSTEM_ADMIN` | Staff (technical) | Full technical administration: accounts, roles, configuration, integrations. Not automatically entitled to academic/finance approvals. |
| `HEAD_OF_SCHOOL` | Staff | Final approvals (results, promotions, admissions, adjustments); school-wide read access. |
| `SCHOOL_ADMIN` | Staff | Front-office administration: student records, admissions processing, class setup, announcements. |
| `HOD` | Staff | Department scope: approves results, oversees teachers and classes in the department. |
| `TEACHER` | Staff | Marks attendance and enters scores for own classes/subjects; class-teacher duties where assigned. |
| `ACCOUNTANT` | Staff | Fee schedules, invoicing, payments, financial reports. |
| `LIBRARIAN` | Staff | Library catalog and loans. |
| `NURSE` | Staff | Health profiles and medical visits. |
| `GUARDIAN` | Guardian | Read access to own wards (results, attendance, invoices, announcements); pays fees; updates own contact info. |
| `STUDENT` | Student (JHS only) | Read own results/timetable/library loans; submit LMS work (post-MVP). |

**Deliberate splits:** `SYSTEM_ADMIN` vs `HEAD_OF_SCHOOL` separates technical power from institutional authority (least privilege). `SCHOOL_ADMIN` exists so front-office staff don't need the Head's approval rights.

## 3. Permission Matrix

Legend: ✓ = full within scope · R = read-only within scope · — = none.
Scopes: **All** school-wide · **Dept** own department · **Own** own classes/subjects (staff) or own wards/self (guardian/student).

| Capability | SYS_ADMIN | HEAD | SCH_ADMIN | HOD | TEACHER | ACCT | LIBR | NURSE | GUARDIAN | STUDENT |
|---|---|---|---|---|---|---|---|---|---|---|
| User accounts & roles | ✓ All | R All | — | — | — | — | — | — | — | — |
| Academic year/term setup | ✓ | ✓ | R | R | R | R | R | R | — | — |
| Student records (bio, docs) | R | ✓ All | ✓ All | R Dept | R Own | R All (billing fields) | R (identity only) | R (identity only) | R Own | R self |
| Guardian records & links | R | ✓ | ✓ | R Dept | R Own | R | — | R | ✓ self (contact only) | — |
| Admissions processing | — | ✓ approve | ✓ process | R | — | R (fees) | — | — | — | — |
| Class & subject setup | R | ✓ | ✓ | R Dept | R Own | — | — | — | — | — |
| Teacher-subject assignment | — | ✓ | ✓ | ✓ Dept | R Own | — | — | — | — | — |
| Attendance mark | — | — | — | — | ✓ Own | — | — | — | — | — |
| Attendance view | R | R All | R All | R Dept | R Own | — | — | R (visit context) | R Own | R self |
| Attendance correction (past day) | — | ✓ | ✓ | ✓ Dept | request | — | — | — | — | — |
| Score entry | — | — | — | — | ✓ Own | — | — | — | — | — |
| Result approval | — | ✓ publish | — | ✓ Dept | — | — | — | — | — | — |
| Results view | R | R All | R All | R Dept | R Own | — | — | — | R Own (published) | R self (published) |
| Promotion decisions | — | ✓ approve | propose | ✓ Dept propose | input | — | — | — | — | — |
| BECE registration | — | ✓ approve | ✓ process | R (JHS) | — | — | — | — | R Own | R self |
| Fee schedules & adjustments | — | ✓ approve | — | — | — | ✓ propose | — | — | — | — |
| Invoicing & payments | — | R All | — | — | — | ✓ All | — | — | R Own / pay | — |
| Financial reports | — | R All | — | — | — | ✓ All | — | — | — | — |
| Timetable manage | — | ✓ | ✓ | ✓ Dept | R Own | — | — | — | R Own | R self |
| Library catalog/loans | — | R | — | — | R | — | ✓ All | — | R Own | R self |
| Health profiles & visits | — | R summary | — | — | ⚠ alerts only | — | — | ✓ All | ✓ Own (view+supply) | — |
| Staff records | R | ✓ All | ✓ All | R Dept | R self | R self | R self | R self | — | — |
| Inventory | R | R | ✓ | R | — | R | — | — | — | — |
| Announcements | ✓ | ✓ | ✓ | ✓ Dept | ✓ Own class | — | — | — | R | R |
| Dashboards/analytics | ✓ tech | ✓ All | R All | R Dept | R Own | ✓ finance | R library | R health | R Own | — |
| Audit log | ✓ | R All | — | — | — | — | — | — | — | — |

⚠ **Teacher health access** is limited to *critical-alert flags* (e.g. "allergic to peanuts", asthma) surfaced on class lists — never the full medical history (BR-HE-001).

## 4. Permission Naming Standard

`<DOMAIN>_<ACTION>` uppercase snake, e.g. `STUDENT_CREATE`, `RESULT_PUBLISH`, `PAYMENT_RECORD`, `ADJUSTMENT_APPROVE`, `AUDIT_VIEW`. Actions: `VIEW`, `CREATE`, `UPDATE`, `DELETE` (rare — see soft-delete policy BR-SE-004), `APPROVE`, `PUBLISH`, `EXPORT`.

**The definitive catalog lives in [14 — Implementation Plan §6](14-implementation-plan.md#6-permission-catalog)** and traces 1:1 to the matrix above; it is seeded by migration and extended (never edited in place). Scope is *not* encoded in the permission string — it is applied per role in the service layer (doc 11 §3).

## 5. Account Lifecycle Rules

- Accounts are **provisioned, never self-registered** (BR-SE-003). Admissions/HR flows trigger account creation; credentials delivered via SMS/email.
- Guardian accounts activate when their first ward is enrolled; deactivate (not delete) when the last ward exits.
- Staff accounts deactivate on employment end. Deactivation revokes tokens immediately.
- Password reset via OTP to registered phone (primary) or email. No security questions.
