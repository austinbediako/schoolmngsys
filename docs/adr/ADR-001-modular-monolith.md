# ADR-001 — Modular Monolith over Microservices

**Status:** Accepted · 2026-07-28

## Problem
Choose the macro-architecture for a 17-module school management system built by a small team for a single school.

## Context
One school (~1–2k students), modest concurrency (NFR-01..04), strongly relational domain with heavy cross-context reads (a report card touches academics, enrollment, attendance, assessment, people). Team is small; operational budget is a single deployment.

## Options
1. **Microservices per module** — independent deployability; but distributed transactions for core flows (enrollment→billing→notification), N databases, infra overhead far beyond need.
2. **Single unstructured monolith** — fastest start; historically decays into tangled dependencies exactly as module count grows past ~5.
3. **Modular monolith** — one deployable, one database, hard internal boundaries (packages + dependency rules + events), extraction possible later.

## Decision
Option 3. Boundaries and rules in [08 — Module Architecture](../08-module-architecture.md); events as seams (doc 02 §4).

## Trade-offs
(+) Transactional integrity across modules; single deploy/backup/monitor; refactoring speed.
(−) Boundary discipline is by convention/review (mitigate: ArchUnit candidate, NFR-13); whole-app redeploys; single-runtime scaling only (acceptable at this scale, revisit per [Roadmap](../13-roadmap.md)).

## Future Implications
If one module ever needs independent scaling (e.g. communication fan-out), its event-driven seam makes it the first extraction candidate without domain rewiring.
