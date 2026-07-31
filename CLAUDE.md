# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.
It is the **engineering constitution** — *how* we build.

**Starting a session? Read [CONTEXT.md](CONTEXT.md) first** — it defines the tiered loading order that reconstructs full
project understanding from the repository alone, plus which document owns which knowledge. Full docs catalog:
[docs/README.md](docs/README.md).

## Project Overview

**UBS-LMIS** — University Basic School Legon Integrated Management Information System. Backend-first school management platform for a Ghanaian basic school (Nursery → JHS 3): students, guardians, staff, attendance, SBA+exam results, BECE, fees, communication. Vision and MVP boundary: [docs/01-product-vision.md](docs/01-product-vision.md). Persistent project memory: [memory.md](memory.md). Current state and next steps: [task.md](task.md).

**Current phase: Phase 1 (MVP backend) in progress — WP-0 through WP-9 complete (M1/M2/M3/M4 closed, M5 in progress), WP-10 (analytics) next.** A prior session's claim of "Phase 1 complete" was verified false 2026-07-31 (see task.md/memory.md) — real WP-10 was never built, and out-of-plan work (BECE, admin bootstrap) was mislabeled as WP-10/WP-11 and shipped with real defects, since fixed. Follow the work-package order, migration sequence, and permission catalog in [docs/14-implementation-plan.md](docs/14-implementation-plan.md) — current state in [task.md](task.md).

## Stack

- Java 21, Spring Boot 4.1 (Maven), PostgreSQL, Flyway, Spring Security + JJWT 0.12.6, Lombok, Actuator.
- Note: Spring Boot 4.x split the `web` starter into `spring-boot-starter-webmvc`, and test starters are per-module (e.g. `spring-boot-starter-data-jpa-test`). Check Spring Boot 4 docs before assuming Boot 2/3-era APIs apply.
- Postgres via `docker-compose.yml` for local development. Frontend (Next.js) comes later (ADR-009) — this repo stays headless.

## Commands

```bash
# Start local Postgres (db: schoolmngsys / user: schoolmngsys / password: schoolmngsys)
docker compose up -d

# Run the app (reads DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD/JWT_SECRET etc. from env, see .env.example)
./mvnw spring-boot:run

# Run with the dev profile (verbose SQL/app logging, see application.yml)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Build
./mvnw clean package

# Run all tests
./mvnw test

# Run a single test class / method
./mvnw test -Dtest=SchoolMngSysApplicationTests
./mvnw test -Dtest=SchoolMngSysApplicationTests#contextLoads
```

## Configuration

- `src/main/resources/application.yml` is the single config file (no `application.properties`); default profile + `dev` profile.
- Environment-specific values via env vars with defaults — `.env.example` lists them. Spring Boot does not read `.env` automatically.
- `spring.jpa.hibernate.ddl-auto=validate` is permanent policy (ADR-005): schema changes go through Flyway migrations in `src/main/resources/db/migration` (`V<seq>__<snake_description>.sql`; directory created with the first migration). Any new JPA entity needs its migration before the app starts.

## Architecture Philosophy

Modular monolith (ADR-001): feature modules under `com.drakalabs.schoolmngsys.*`, each layered `api / domain / service / repository`. Boundary rules and the module dependency map are **binding**: [docs/08-module-architecture.md](docs/08-module-architecture.md). Cross-module writes via owning module's services; cross-module notification via domain events; external messaging via transactional outbox (ADR-008). All architectural decisions have ADRs in [docs/adr/](docs/adr/) — a new architectural decision means a new ADR *before* the code.

## Decision-Making Rules

1. **Documented rule exists** (BR-/FR-/NFR-/ADR-) → follow it; cite the ID in PRs/commits where relevant.
2. **Requirement unclear** → do not invent. Record an assumption in [docs/04-business-rules.md](docs/04-business-rules.md) assumptions index (A-nn) with owner, note it in [task.md](task.md), proceed on the documented default.
3. **Conflict between docs** → the canonical owner wins (see [docs/README.md](docs/README.md) map); fix the stale doc in the same change.
4. **Architectural change** → ADR first (Problem / Context / Options / Decision / Trade-offs / Future Implications), then implementation.
5. Keep `memory.md` and `task.md` current as part of the change that affects them — never as an afterthought.

## Documentation Discipline

This repository is **documentation-driven** (ADR-011): conversation history is temporary, the repository is permanent.
Any knowledge that would otherwise exist only in a chat log — a rule, a rationale, an assumption, a rejected option —
must be persisted before the session ends. Protocol, ownership registry, and the end-of-session checklist live in
[CONTEXT.md](CONTEXT.md); the essentials:

- **One canonical home per topic.** Reference by ID (`BR-`, `FR-`, `NFR-`, `WF-`, `G-`, `A-`, `ADR-`, `WP-`); never copy prose between documents. Finding the same fact in two places is a defect to consolidate.
- **Documentation changes ship with the change that caused them**, not afterwards. Architecture → [docs/08](docs/08-module-architecture.md) + an ADR; rules → [docs/04](docs/04-business-rules.md); practice → this file; knowledge → [memory.md](memory.md); status → [task.md](task.md).
- **Never assume silently.** An unclear requirement becomes a recorded `A-nn` assumption with a named owner, not an invented feature. When it is confirmed, update the assumption row and every gap that depended on it.
- **Self-heal as you go.** Fix contradictions, stale statements, duplication, inconsistent terms, broken references, requirements with no architectural home, and architecture with no requirement behind it.
- **Leave the repository more understandable than you found it** — this is a completion criterion, not an aspiration.

## Engineering Standards (high level)

- **Non-negotiables** (full list in [memory.md](memory.md)): history is never destroyed (year-scoped enrollments, immutable receipts, result revisions, append-only audit); every business rule is enforced server-side; children's-data least privilege everywhere; no self-registered accounts.
- **Naming:** DB per [docs/09 §1](docs/09-data-architecture.md); API per [docs/10](docs/10-api-standards.md); permissions per [docs/03 §4](docs/03-roles-and-permissions.md); Java: standard conventions, module packages per docs/08; migrations immutable once merged.
- **Database principles:** UUIDv7 surrogate PKs + separate business numbers; constraints in the schema for stated invariants (docs/09 §4); `created/updated_at/by` on every domain table; soft delete via `archived_at`; money `numeric(12,2)` GHS; timestamps UTC `timestamptz`.
- **API principles:** REST `/api/v1`, plural kebab-case resources, camelCase JSON, RFC 7807 problems carrying `ruleId` for business-rule rejections, pagination caps, `Idempotency-Key` on financial/bulk mutations, optimistic concurrency. Full standards: [docs/10](docs/10-api-standards.md). Every endpoint validates input (Bean Validation at DTO boundary) — also mandated by `.claude/rules/Api.md`.
- **Error-handling philosophy:** fail loudly with typed problems; semantic failures cite BR IDs; never leak SQL/stack traces; constraint violations are the last net, not the first check; workflows never fail because notifications failed (outbox absorbs).
- **Security principles:** permission gate + scope filter + field-level DTO shaping on every access path (docs/11 §3); no PII in logs or URLs; secrets via env only. Threat model and DPA obligations: [docs/11](docs/11-security-and-privacy.md).
- **State management philosophy:** the database is the only state; services stateless; the sole deliberate server state is hashed refresh tokens (ADR-004) and the notification outbox (ADR-008). "Current" anything (class, teacher, year) is a query over year-scoped rows, never a mutable pointer (ADR-006).
- **UI principles (for the future frontend):** clients render, backend decides; API responses must be complete enough that clients never re-implement business math (NFR-18); low-bandwidth-friendly payloads (NFR-19); WCAG 2.1 AA target.

## Conventions

- Error responses follow RFC 7807 format (see docs/10 §2).
- Frontend code (when the frontend phase begins): use async/await over raw promises; named exports preferred over defaults.

## Testing Philosophy

Domain logic that computes or gates (result computation, positions, payment allocation, promotion rules, scope filters) gets exhaustive unit tests — these are the school's trust surface. Web/persistence via slice tests; one integration test per workflow happy-path + its edge cases from [docs/07](docs/07-workflows.md). Persistence tests run against real PostgreSQL (Testcontainers/compose), never H2. Fixtures use synthetic Ghanaian-realistic data, never production data.

## Review Checklist (apply to every PR)

1. Cites/uses the relevant BR/FR IDs; no undocumented behavior introduced.
2. Module boundaries respected (no foreign repositories, no entity leaks into `api`, no DTO leaks into `service`).
3. New/changed entities ship with their Flyway migration and constraint coverage per docs/09 §4.
4. Mutations audited; scope filters present on new read paths; no PII in logs/URLs.
5. RFC 7807 + validation on new endpoints; OpenAPI still generates.
6. Tests per the philosophy above; all green against real Postgres.
7. Docs updated in the same PR: canonical doc for any rule/architecture change, an ADR for any decision, `memory.md`/`task.md` if affected, assumptions recorded — cross-references and anchors still resolve.

## Definition of Done

Code + migration + tests + audit/scope coverage + documentation updated per the discipline above + review checklist passed + [CONTEXT.md end-of-session checklist](CONTEXT.md#4-end-of-session-checklist) satisfied + runs clean via the Commands above against compose Postgres.

## Glossary

Canonical domain language lives in [docs/glossary.md](docs/glossary.md) — use those terms exactly, in code, docs, and conversation (Enrollment, Term Result, SBA, Class Position, Guardian/Ward, Fee Schedule, …). If a needed term is missing, add it there first.
