# ADR-010 — Single-Tenant Design (One School, No school_id)

**Status:** Accepted · 2026-07-28

## Problem
Should the schema and code carry a `school_id`/tenant dimension "just in case" UBS-LMIS is later sold to other schools?

## Context
The concept document scopes the system to University Basic School, Legon — one school, one calendar, one fee structure authority. Tenanting every table and query adds a permanent tax (composite keys, scope bugs, test surface) for a hypothetical.

## Options
1. Multi-tenant now (school_id everywhere). 2. **Single-tenant now; multi-school later via separate deployments or a real multi-tenancy project.**

## Decision
Option 2 (YAGNI, explicit over speculative). No tenant columns. School-level settings (name, branding, policy toggles like BR-FI-007) live in a single configuration aggregate — one place a future tenant dimension would attach.

## Trade-offs
(+) Simpler schema, queries, authorization, tests; zero tenant-leak risk.
(−) A future SaaS pivot means real migration work — accepted and documented in [13 — Roadmap](../13-roadmap.md); UUID keys (doc 09 §2) at least make data merges feasible.

## Future Implications
If multi-school ever becomes real, the modular monolith's service seams localize the change; per-school deployment remains the cheap interim answer.
