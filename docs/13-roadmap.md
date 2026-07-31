# 13 — Roadmap: Scalability, Deployment & Future Expansion

> Phases are capability milestones, not calendar promises. MVP boundary: [01 §5](01-product-vision.md#5-mvp-boundary);
> deferred items reference [12 — Gap Analysis](12-gap-analysis.md).

## Phase 0 — Foundation (this phase; documentation)
Engineering docs (this set) → stakeholder review of the assumptions index → implementation planning.

## Phase 1 — MVP Backend
Modules: auth, academics, people, enrollment (direct), attendance, assessment, promotion run, finance core, communication core, head dashboard, audit. Deployment: single VM/container host, dockerized app + PostgreSQL, TLS reverse proxy, daily backups (NFR-06). Exit criterion: one full term operated digitally in parallel with paper.

## Phase 2 — First Frontend + Operational Depth
Next.js web app (guardian portal + staff consoles) consuming `/api/v1`; CORS/asymmetric-signing revisit (ADR-004). Admissions workflow, absence SMS, role dashboards, MoMo online payments (provider integration), staff leave.

## Phase 3 — Full Academic Cycle
Timetable, BECE cycle (registration export, mock series, results import), graduation/leaving records, LMS-lite, library, health, inventory. First year-end promotion run in production validates ADR-006.

## Phase 4 — Insight & Reach
Longitudinal analytics (FR-DASH-03), audit anomaly alerts (ADR-007 implication), two-way messaging/WhatsApp adapter (ADR-008), offline-capable attendance client (G-20), alumni/transcript service (G-21), ID cards (G-12), discipline module (G-10).

## Scaling Path (trigger-based, per ADRs)

| Trigger | Step |
|---|---|
| Dashboard/report load hurts interactive traffic | Materialized views → PostgreSQL read replica (ADR-002) |
| Notification fan-out delays workflows | Extract communication module behind its event seam (ADR-001) |
| Second token consumer (SSR frontend, mobile) | Asymmetric JWT signing (ADR-004) |
| Multi-school interest | Per-school deployments first; true multi-tenancy only as a funded project (ADR-010) |
| University SSO mandate | OIDC federation for staff accounts (ADR-004 implication) |

## Standing Constraints for All Phases
Every phase respects: Flyway-only schema change (ADR-005), audit completeness (ADR-007), API standards (doc 10), DPA posture (doc 11 §4), and the module dependency map (doc 08 §3). Roadmap changes are made by editing this file plus an ADR when a decision is architectural.
