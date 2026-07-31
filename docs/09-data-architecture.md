# 09 — Data Architecture

> PostgreSQL is the single system of record (ADR-002); schema evolves only via Flyway (ADR-005).
> Conceptual entities: [02 — Domain Model](02-domain-model.md). This document sets persistence standards
> and planning; **it contains no DDL** — migrations are an implementation-phase artifact.

## 1. Naming Standards (canonical)

| Element | Convention | Example |
|---|---|---|
| Tables | `snake_case`, plural | `students`, `term_results`, `payment_allocations` |
| Columns | `snake_case` | `date_of_birth`, `weighted_total` |
| Primary keys | `id` | — |
| Foreign keys | `<singular>_id` | `student_id`, `academic_year_id` |
| Join tables | both names, alphabetical | `student_guardians` |
| Indexes | `idx_<table>_<cols>` | `idx_attendance_records_enrollment_id_date` |
| Unique constraints | `uq_<table>_<cols>` | `uq_enrollments_student_year_active` |
| Check constraints | `ck_<table>_<rule>` | `ck_scores_raw_score_range` |
| Enums | PostgreSQL `text` + check constraint (not native enums) — migration-friendly | `status text` + `ck_...` |
| Migrations | `V<seq>__<snake_description>.sql`, immutable once merged | `V3__create_enrollment_tables.sql` |
| Money | `numeric(12,2)`, currency implicitly GHS (single-currency system) | `amount` |
| Timestamps | `timestamptz`, UTC; column names `<verb>_at` | `published_at` |
| Dates (calendar concepts) | `date` (school days, DOB) | `attendance_date` |

## 2. Identifier Strategy

- **Surrogate PKs:** UUIDv7 (time-ordered, index-friendly) for all domain tables. Rationale: safe external exposure (no enumerable integer IDs leaking student counts — child-data caution), mergeable across environments; v7 avoids the index-locality penalty of v4.
- **Business identifiers** (student number, staff number, receipt number, invoice number) are separate, unique, human-facing columns with generation rules (BR-EN-002, BR-FI-003), never reused as PKs/FKs.

## 3. Cross-Cutting Column Conventions

Every domain table carries: `created_at`, `updated_at`, `created_by`, `updated_by` (account id). Soft-deletable tables add `archived_at` (BR-SE-004) — partial unique indexes must exclude archived rows. No table stores computed values that can drift silently — computed snapshots (e.g. `term_results`) are explicit, versioned, and regenerated only through their workflow.

## 4. Integrity Rules the Schema Must Enforce (not just the application)

| Invariant | Mechanism |
|---|---|
| One active enrollment per student per year (BR-EN-001) | Partial unique index on `enrollments(student_id, academic_year_id) WHERE status = 'ACTIVE'` |
| One attendance record per student per day (BR-AT-001) | Unique `(enrollment_id, attendance_date)` |
| Score bounds (BR-AA-002) | Check `raw_score BETWEEN 0 AND max_score` (via component join at app level + non-negative check at DB) |
| One class-teacher per class per year (BR-AS-005) | Unique `(class_id, academic_year_id)` on assignment table |
| No timetable double-booking (BR-TT-001) | Unique `(teacher_id, term_id, day, period)` and `(class_id, term_id, day, period)` |
| Receipt immutability (BR-FI-003) | No UPDATE path in application; DB trigger raising on UPDATE/DELETE of posted payments *(decide at implementation; documented option)* |
| Exactly 3 terms per year (BR-AS-001) | Application-enforced + check on `term_number IN (1,2,3)` unique per year |

## 5. History & Temporal Modeling

- **Year-scoped associations** (enrollment, class-teacher, subject-teacher, HoD) are rows-with-year, never mutable "current" pointers. "Current" is always a query (`WHERE academic_year = active`).
- **Approval pipelines** (results) persist status + status-transition timestamps + actor per transition (either columns or a transition table — decide per volume; results likely columns, audit log covers the rest).
- **Revisions** (BR-AA-006): `term_results` keeps `version`; superseded rows remain, flagged `superseded_by`.

## 6. Audit Log Design

Single `audit_log` table, append-only: `id, occurred_at, actor_account_id, action, entity_type, entity_id, summary jsonb, ip`. Written in the same transaction as the mutation (listener/aspect at service layer). No FK from audit to domain tables (audit outlives archived data). Partition by year when volume warrants (NFR-04) — design allows, don't pre-build.

## 7. Read Models & Reporting

Dashboards (FR-DASH-*) read from SQL views or materialized views owned by `analytics`, refreshed on schedule or event — never ad-hoc cross-module joins scattered through code. Report-card data assembly is a service-layer composition in `assessment` (interactive path), not a view.

## 8. Data Lifecycle & Retention

Canonical table in [07 — Workflows, Data Lifecycle](07-workflows.md#data-lifecycle-summary). Schema implications: `archived_at` everywhere retention applies; erasure requests (BR-SE-004) implemented as targeted anonymization scripts per entity type (documented procedures, not casual DELETEs).

## 9. Migration Discipline (ADR-005)

1. `ddl-auto=validate` stays forever; Hibernate never generates schema.
2. One migration per logical change-set; expand-migrate-contract pattern for breaking changes (add column → backfill → swap → drop in later release).
3. Reference data (class levels, subjects, permissions, default grade scale) is seeded by migrations (`V*__seed_*.sql`) — environment-independent, deterministic.
4. Migrations are immutable post-merge; corrections are new migrations.
5. Every entity PR includes its migration and a test run against real PostgreSQL (Testcontainers or compose DB, not H2 — dialect fidelity matters).
