# 06 — Non-Functional Requirements

> IDs `NFR-nn`. Where a target is a guess, it is marked and should be revisited with real usage data.
> Security specifics live in [11 — Security & Privacy](11-security-and-privacy.md); this file holds the targets.

## Context that shapes the NFRs

- **Scale is modest and predictable**: one school, roughly 1,000–2,000 students, ~100 staff, ~2,500 guardian accounts, seasonal peaks (result publication days, fee deadlines, admission season). This justifies a modular monolith (ADR-001) and rules out premature distributed-systems complexity.
- **Ghana operating reality**: intermittent power/network at times; guardians on mobile data with mid-range phones; SMS more reliable than email; school hours are the critical availability window.

## Performance & Capacity

| ID | Requirement | Target |
|---|---|---|
| NFR-01 | API latency, interactive reads | p95 < 500 ms under normal load *(guess — validate)* |
| NFR-02 | Bulk operations (billing run, promotion run, result computation per class) | Complete within minutes, run async with progress status; never block interactive traffic |
| NFR-03 | Result-publication burst (all guardians checking at once) | Sustain ~100 concurrent users without degradation |
| NFR-04 | Data volume planning | 10-year horizon: low millions of rows in the largest tables (attendance, scores, audit) — comfortably PostgreSQL territory; indexing strategy in [09](09-data-architecture.md) |

## Availability & Recovery

| ID | Requirement | Target |
|---|---|---|
| NFR-05 | Availability window | Best effort 24/7; **critical**: school days 06:00–18:00 GMT. Maintenance in off-hours. |
| NFR-06 | Backups | Automated daily full backup + WAL/point-in-time capability; restore tested each term |
| NFR-07 | RPO / RTO | RPO ≤ 24 h (target ≤ 1 h with WAL), RTO ≤ 4 h *(guess — align with school tolerance)* |
| NFR-08 | Graceful degradation | If SMS provider is down, notifications queue in outbox and retry — domain workflows never fail because messaging failed |

## Security & Privacy (targets; details in doc 11)

| ID | Requirement |
|---|---|
| NFR-09 | OWASP ASVS Level 2 alignment for the API |
| NFR-10 | Ghana Data Protection Act (Act 843) compliance posture: lawful basis documentation, data minimization, subject access/erasure procedures — children's data throughout |
| NFR-11 | All traffic TLS; passwords bcrypt/argon2; secrets never in the repository (env-based, `.env.example` documents keys) |
| NFR-12 | Complete auditability of domain mutations (BR-SE-002) with actor attribution |

## Maintainability & Quality

| ID | Requirement |
|---|---|
| NFR-13 | Module boundaries enforced per [08](08-module-architecture.md); no cross-module repository access |
| NFR-14 | Test pyramid: unit tests on domain logic (result computation, allocation, promotion rules are 100%-covered candidates), slice tests for web/persistence, focused integration tests per workflow |
| NFR-15 | Every schema change via Flyway migration; migrations never edited after merge |
| NFR-16 | API documented via OpenAPI, generated from code, reviewed against [10 — API Standards](10-api-standards.md) |
| NFR-17 | Structured logging with correlation IDs; no PII in log lines (log IDs, not names) |

## Usability & Accessibility (constrains the future frontend; API must not preclude them)

| ID | Requirement |
|---|---|
| NFR-18 | WCAG 2.1 AA for the eventual web UI; API responses provide all data needed without client-side business computation |
| NFR-19 | Low-bandwidth friendliness: pagination everywhere, sparse payloads, no mandatory large assets; photos served resized |
| NFR-20 | English UI language; names/fields must handle Ghanaian names and phone formats (+233 normalization) |
| NFR-21 | Timezone: single-zone system, Africa/Accra (GMT year-round); all dates stored UTC, rendered in school zone |

## Operability

| ID | Requirement |
|---|---|
| NFR-22 | Health/readiness endpoints (Actuator) for supervision; metrics for request rates, queue depth (outbox), job status |
| NFR-23 | Single-command local environment (docker compose) reproducing production topology (app + PostgreSQL) |
| NFR-24 | Deployment target flexibility: containerized app deployable to a single VM initially (see [13 — Roadmap](13-roadmap.md) for evolution) |
