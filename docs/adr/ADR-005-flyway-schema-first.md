# ADR-005 — Flyway Schema-First, Hibernate Validate-Only

**Status:** Accepted · 2026-07-28 (ratifies existing configuration)

## Problem
Who owns the schema: ORM auto-generation or explicit migrations?

## Context
`application.yml` already sets `ddl-auto: validate`; Flyway + postgres plugin already dependencies. Academic/financial data is legally significant — schema changes must be reviewable artifacts.

## Decision
Flyway owns the schema; Hibernate only validates. Discipline in [09 §9](../09-data-architecture.md): immutable versioned migrations, expand-migrate-contract for breaking changes, reference data seeded via migrations, tests against real PostgreSQL.

## Trade-offs
(+) Reviewable, replayable schema history; environments provably identical; constraints (doc 09 §4) live where they're enforced.
(−) Slower iteration than `ddl-auto=update` in early dev — accepted; correctness over convenience for this data.

## Future Implications
Zero-downtime deploys later require the expand/contract habit — adopting it from day one avoids retraining.
