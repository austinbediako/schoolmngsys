# 14 — Phase 1 Implementation Plan (MVP Backend)

> Planning artifact — **still no application code**. This document sequences the MVP build so implementation
> can proceed without re-deciding architecture. Scope = MVP per [01 §5](01-product-vision.md#5-mvp-boundary)
> and the feature matrix in [05](05-functional-requirements.md#feature-matrix).
> Prerequisite met: all assumptions A-01…A-11 confirmed 2026-07-29.

## 1. Sequencing Principles

1. **Walking skeleton first.** One thin end-to-end slice (login → read a class roster) proves stack, security, migrations, error format, and test harness before breadth is added.
2. **Dependency order, not module-list order.** Build in the direction of the dependency map ([08 §3](08-module-architecture.md#3-module-dependency-map)): nothing is built before what it reads from exists.
3. **Cross-cutting concerns are infrastructure, not features.** Audit, RFC 7807 problems, validation, pagination, scope filtering are built once in WP-0 and inherited — never retrofitted per module.
4. **Every work package is independently shippable and verifiable** against the Definition of Done in [CLAUDE.md](../CLAUDE.md).
5. **Operate-a-term ordering.** Milestones follow the school's real sequence (set up year → enroll → daily ops → close term), so partial delivery still has value.

## 2. Work Packages

Dependencies are on other work packages. "Rules" cites the binding rules the package must enforce.

| WP | Package | Depends on | Delivers | Rules |
|---|---|---|---|---|
| **WP-0** | Shared foundations | — | Base entity conventions (id/audit columns), RFC 7807 problem handling + problem catalog, validation wiring, pagination envelope, domain-event publishing, **audit interception**, Testcontainers harness, ArchUnit boundary tests | doc 09 §3, doc 10 §2–3, BR-SE-002 |
| **WP-1** | auth | WP-0 | Accounts, roles, permissions, JWT issue/refresh/rotation, lockout, OTP reset, permission gate + scope-filter infrastructure | FR-AUTH-01…05, BR-SE-003/005, ADR-004 |
| **WP-2** | academics | WP-1 | Academic years/terms, calendar variants, school-day calendar, departments, class levels, classes, subjects, per-year offerings and teacher assignments | FR-ACAD-01…05, BR-AS-001…007 |
| **WP-3** | people | WP-1 | Students, guardians, student-guardian links, staff registry, document attachments; **guardian-ward resolution used by every scope filter** | FR-STU-01/02/05, FR-STF-01/02, BR-EN-004, BR-ST-001/002 |
| **WP-4** | enrollment | WP-2, WP-3 | Enrollments, roster queries, exit handling (transfer/withdrawal) | FR-STU-03/04, BR-EN-001…005, ADR-006 |
| **WP-5** | attendance | WP-4 | Bulk daily register, school-day validation, corrections with reason, summaries | FR-ATT-01…04, BR-AT-001…005 |
| **WP-6** | assessment | WP-4 | Grade scales, assessment components, bulk score entry, result computation (totals/grades/positions), approval pipeline, report-card data, revisions | FR-RES-01…07, BR-AA-001…008 |
| **WP-7** | finance | WP-4 | Fee schedules, billing run, invoices, payments + immutable receipts, allocation, adjustments, arrears, reports | FR-FIN-01…06, BR-FI-001…007 |
| **WP-8** | communication | WP-6, WP-7 | Outbox, templates, SMS/email adapters, event subscriptions, announcements, delivery log | FR-COM-01…03, BR-CO-001…004, ADR-008 |
| **WP-9** | progression | WP-6 | Year-end promotion run, repeat exceptions with approval, bulk next-year enrollment generation | FR-PRO-01/02, BR-PR-001…005 |
| **WP-10** | analytics | WP-5, WP-6, WP-7 | Head dashboard read models (enrollment, attendance rate, collection, results distribution) | FR-DASH-01 |

**Not in Phase 1** (per feature matrix): admissions workflow, BECE, timetable, LMS, library, health, inventory, staff leave, role dashboards, MoMo online payments.

## 3. Milestones

| Milestone | Contents | Proves |
|---|---|---|
| **M1 — Walking skeleton** | WP-0 + WP-1 + minimal slice of WP-2/WP-3/WP-4 (one year, one class, a few students, roster endpoint) | Stack, auth, migrations, error format, audit, tests all work end to end |
| **M2 — Registry ready** | WP-2, WP-3, WP-4 complete | School can be modeled and populated; scope filters proven on real data |
| **M3 — Daily operations** | WP-5, WP-8 (core notifications) | A teacher can run a school day; guardians receive messages |
| **M4 — Term close** | WP-6, WP-7 | A full term can be assessed, published, billed, and receipted |
| **M5 — MVP complete** | WP-9, WP-10, hardening | Year-end promotion runs; Head has dashboards; parallel-run with paper begins |

M5 exit = Phase 1 exit criterion from [13 — Roadmap](13-roadmap.md): *one full term operated digitally in parallel with paper.*

## 4. Migration Sequence (scope only — SQL is written during implementation)

Naming per [09 §1](09-data-architecture.md#1-naming-standards-canonical); immutable once merged (ADR-005).

| Migration | Scope |
|---|---|
| `V1__shared_foundations` | UUID generation support; `audit_log` table (append-only, doc 09 §6) |
| `V2__auth` | accounts, roles, permissions, role_permissions, account_roles, refresh_tokens, login_attempts |
| `V3__seed_permissions_and_roles` | Permission catalog (§6) and role bundles from [03](03-roles-and-permissions.md) |
| `V4__academic_reference_data` | class_levels (ordered ladder, Basic-numbering aliases), departments, subjects (NaCCA list with level applicability) — seeded, not user-editable (doc 02 §5) |
| `V5__academic_structure` | academic_years, terms, term_calendar_variants, school_day_exceptions, classes, class_teacher_assignments, class_subject_offerings |
| `V6__people` | students, guardians, student_guardians, staff, staff_qualifications, person_documents |
| `V7__enrollment` | enrollments (+ partial unique index enforcing BR-EN-001) |
| `V8__attendance` | attendance_records (+ unique per enrollment/date, BR-AT-001) |
| `V9__assessment` | grade_scales, grade_bands, assessment_components, scores, term_results (versioned), report_cards |
| `V10__seed_default_grade_bands` | Confirmed A-04 bands as a year-independent *template* (`default_grade_bands`, no academic_year_id — no year exists at migration time); `GradeScaleService.createDefault(academicYearId)` copies it into a real per-year `grade_scales`/`grade_bands` set with the A-03 30/70 weighting, as an explicit WF-01 step-5 admin action, not an automatic cascade |
| `V11__finance` | fee_schedules, fee_items, invoices, invoice_lines, adjustments, payments, payment_allocations |
| `V12__communication` | message_templates, notification_outbox, notification_deliveries, announcements |
| `V13__progression` | promotion_runs, promotion_decisions |
| `V14__analytics_read_models` | Views/materialized views for FR-DASH-01 |

Constraint coverage for the invariants in [09 §4](09-data-architecture.md#4-integrity-rules-the-schema-must-enforce-not-just-the-application) is part of the migration that creates each table — not a later pass.

## 5. Testing Plan per Work Package

| WP | Must-have tests |
|---|---|
| WP-0 | Problem-format contract test; audit-written-on-mutation test; ArchUnit rules (no cross-module repository access, no entity in `api`, `shared` depends on nothing) |
| WP-1 | Token issue/refresh/rotation; revocation on deactivation (FR-AUTH-04); lockout; permission gate denies without permission; scope-filter unit tests |
| WP-2 | Three-terms invariant; calendar-variant resolution (JHS 3 dates differ); school-day computation across holidays; one-class-teacher-per-year (A-01) |
| WP-3 | Guardian-ward resolution correctness (feeds all guardian scoping); ≥1 primary contact rule (BR-EN-004); immutable student number (BR-EN-002, format A-05) |
| WP-4 | One-active-enrollment-per-year (DB + service); mid-year transfer-in/out; roster excludes exited students but history intact |
| WP-5 | Non-school-day rejection; duplicate-day rejection; LATE/EXCUSED aggregation semantics (A-07); post-day correction requires permission + reason |
| WP-6 | **Highest-value suite**: weighted totals at 30/70 (A-03), grade banding (A-04), competition ranking with ties (BR-AA-004), missing-score blocking (BR-AA-007), pipeline role gates (BR-AA-003), published immutability + revision (BR-AA-006) |
| WP-7 | Oldest-first allocation incl. part payments (A-09/BR-FI-002); arrears carry-forward; receipt immutability; reversal correctness; adjustment approval gate |
| WP-8 | Outbox written in same transaction as domain change; retry/backoff; provider outage doesn't fail the workflow (NFR-08); guardian message consolidation |
| WP-9 | Auto-promote default (A-08); repeat requires justification + Head approval; bulk next-year enrollment generation; JHS 3 excluded from promotion |
| WP-10 | Read-model figures match transactional truth for a seeded term |

All persistence tests run against real PostgreSQL (never H2) per [CLAUDE.md](../CLAUDE.md) testing philosophy; fixtures use synthetic Ghanaian-realistic data.

## 6. Permission Catalog

**Definitive list**, tracing 1:1 to the matrix in [03 §3](03-roles-and-permissions.md#3-permission-matrix). Seeded by `V3`. Scope suffixes are *not* encoded in the string; scope is applied per-role in the service layer (doc 11 §3).

| Domain | Permissions |
|---|---|
| Accounts | `ACCOUNT_VIEW`, `ACCOUNT_CREATE`, `ACCOUNT_UPDATE`, `ACCOUNT_DEACTIVATE`, `ROLE_ASSIGN` |
| Academic structure | `ACADEMIC_YEAR_VIEW/CREATE/UPDATE/CLOSE`, `CLASS_VIEW/CREATE/UPDATE`, `SUBJECT_VIEW`, `SUBJECT_OFFERING_MANAGE`, `TEACHER_ASSIGNMENT_MANAGE`, `CALENDAR_MANAGE` |
| Students & guardians | `STUDENT_VIEW`, `STUDENT_VIEW_IDENTITY_ONLY`, `STUDENT_CREATE`, `STUDENT_UPDATE`, `STUDENT_ARCHIVE`, `GUARDIAN_VIEW/CREATE/UPDATE`, `GUARDIAN_LINK_MANAGE`, `STUDENT_DOCUMENT_VIEW/UPLOAD` |
| Enrollment | `ENROLLMENT_VIEW`, `ENROLLMENT_CREATE`, `ENROLLMENT_END`, `ROSTER_VIEW` |
| Attendance | `ATTENDANCE_VIEW`, `ATTENDANCE_MARK`, `ATTENDANCE_CORRECT` |
| Assessment | `ASSESSMENT_COMPONENT_MANAGE`, `SCORE_ENTER`, `RESULT_VIEW`, `RESULT_SUBMIT`, `RESULT_APPROVE`, `RESULT_PUBLISH`, `RESULT_REVISE`, `REPORT_CARD_VIEW`, `GRADE_SCALE_MANAGE` |
| Progression | `PROMOTION_PROPOSE`, `PROMOTION_APPROVE`, `PROMOTION_RUN_EXECUTE` |
| Finance | `FEE_SCHEDULE_VIEW/MANAGE`, `FEE_SCHEDULE_APPROVE`, `INVOICE_VIEW`, `BILLING_RUN_EXECUTE`, `PAYMENT_VIEW`, `PAYMENT_RECORD`, `PAYMENT_REVERSE`, `ADJUSTMENT_PROPOSE`, `ADJUSTMENT_APPROVE`, `FINANCE_REPORT_VIEW` |
| Staff | `STAFF_VIEW`, `STAFF_CREATE`, `STAFF_UPDATE`, `STAFF_END_EMPLOYMENT` |
| Communication | `ANNOUNCEMENT_CREATE`, `ANNOUNCEMENT_VIEW`, `NOTIFICATION_VIEW_OWN`, `MESSAGE_TEMPLATE_MANAGE` |
| Analytics & audit | `DASHBOARD_VIEW_SCHOOL`, `DASHBOARD_VIEW_DEPARTMENT`, `DASHBOARD_VIEW_OWN`, `DASHBOARD_VIEW_FINANCE`, `AUDIT_VIEW` |
| Data export | `EXPORT_EXECUTE` (audited separately, doc 11 §1) |

Post-MVP domains (timetable, library, health, inventory, BECE, LMS) add their permissions when built; the catalog is extended by migration, never edited in place.

## 7. Definition of Ready (before a WP starts)

Its rules are confirmed (no open A-nn), its dependencies are merged, its API resources are sketched against [10](10-api-standards.md), and its test list above is agreed. Anything discovered mid-package that changes a rule stops the package and updates [04](04-business-rules.md) first.

## 8. Open Implementation-Time Decisions (deliberately deferred, not forgotten)

| Decision | When | Notes |
|---|---|---|
| SMS provider selection | Before WP-8 | Adapter seam already fixed (ADR-008); compare Hubtel/Arkesel/Twilio on Ghana delivery + cost |
| ~~Document/photo storage target~~ | **Decided (WP-3, 2026-07-30)** | Filesystem, behind a `DocumentStorage` seam (`people.service`); access-checked streaming (doc 11 §4). Object storage is an additive swap behind the same interface — revisit before M5's hosting decision if scale demands it. |
| ~~Receipt-immutability enforcement mechanism~~ | **Decided (WP-7, 2026-07-30)** | Application-only: `Payment` has no update path in the service layer, and `amount`/`receiptNumber`/`channel` are `updatable = false` at the JPA level so Hibernate can't emit an UPDATE for them even by mistake. Corrections are exclusively new reversal rows (BR-FI-003) — no DB trigger, consistent with every other "immutable" entity in this codebase (`Student.studentNumber`, `TermResult` revisions) using the same application-only pattern, not triggers. |
| Hosting target and backup automation | Before M5 | NFR-06/07/24 |
| Report-card layout | Frontend phase | Data contract (FR-RES-05) is backend's obligation; layout is with the school (G-03) |
