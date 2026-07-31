# ADR-002 — PostgreSQL as the Single Data Store

**Status:** Accepted · 2026-07-28

## Problem
Select persistence technology/-ies for records that are relational, historical, and legally significant.

## Context
Domain is textbook-relational (students×enrollments×results×invoices); volumes low-millions of rows over a decade (NFR-04). Stack already includes PostgreSQL driver + Flyway. Team benefits from one operational surface.

## Options
1. PostgreSQL only. 2. PostgreSQL + Redis cache. 3. PostgreSQL + document store for flexible records (health, documents).

## Decision
PostgreSQL only. `jsonb` covers semi-structured needs (audit summaries, registration snapshots). Files (photos, documents) live on filesystem/object storage with DB metadata — not in the DB, not in a second database. Caching, if ever needed, starts as in-process; Redis only on measured need.

## Trade-offs
(+) One backup/restore story (NFR-06/07), full SQL for reporting, constraint-level integrity (doc 09 §4).
(−) No horizontal write scaling — irrelevant at this scale; hot dashboards may need materialized views (planned, doc 09 §7).

## Future Implications
Read-replica is the first scaling step if reporting load grows (Roadmap).
