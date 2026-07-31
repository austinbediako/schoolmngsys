# 11 — Security & Privacy Architecture

> Targets in [06 — NFRs](06-non-functional-requirements.md); rules BR-SE-* in [04](04-business-rules.md);
> authorization model in [03 — Roles & Permissions](03-roles-and-permissions.md). Decision record: ADR-004.
> **The dominant fact of this system: nearly every record concerns a child.** Design defaults follow from that.

## 1. Threat Model (summary)

| Threat | Vector | Primary mitigations |
|---|---|---|
| Unauthorized access to child data | Credential theft, over-broad roles, scope bypass | Least-privilege matrix, server-side scope filters, short-lived tokens, lockout (FR-AUTH-05) |
| Grade tampering | Teacher/insider edits after the fact | Approval pipeline (BR-AA-003), published-immutability + revisions (BR-AA-006), full audit (BR-SE-002) |
| Financial fraud | Fake receipts, deleted payments, quiet discounts | Immutable receipts + reversal-only (BR-FI-003), approval on adjustments (BR-FI-004), audit, daily cash book report |
| Guardian impersonation | Social engineering of accounts | Provisioned accounts only (BR-SE-003), OTP resets to registered phone, ward-scope enforcement |
| Data exfiltration | Bulk export abuse | `EXPORT` as a distinct audited permission; exports logged with row counts; no anonymous endpoints |
| Injection/web attacks | Standard OWASP surface | Parameterized persistence (JPA), Bean Validation at boundary, security headers, ASVS L2 checklist (NFR-09) |

## 2. Authentication (ADR-004)

- **Stateless JWT access tokens** (short TTL, ~15 min) signed HS256 initially (JJWT already a dependency), key from env (`JWT_SECRET`); asymmetric keys become worthwhile when a second token consumer appears (future frontend SSR/other services).
- **Rotating refresh tokens** persisted server-side (hashed), enabling immediate revocation on deactivation (FR-AUTH-04) — the one deliberate piece of state.
- Login identifiers: staff number / phone / username. Passwords argon2id or bcrypt (NFR-11). Forced change on first login; OTP reset flow.
- Account lockout with exponential backoff after repeated failures; all auth events audited.

## 3. Authorization

Three layers, all server-side:
1. **Permission gate** at endpoint (method security on permission strings from doc 03 §4).
2. **Scope filter** in services: teacher→own classes; HoD→department; guardian→own wards (resolved via `people` module's guardian-ward links); accountant→all finance, no academics.
3. **Field-level shaping** in DTOs: the same student renders differently to Librarian (identity-only) vs Nurse vs Guardian.

Never trust client-supplied scope (e.g. a `classId` param is *validated against* the caller's scope, not taken as authority).

## 4. Privacy by Design (Ghana DPA, Act 843)

| Principle | Concrete practice |
|---|---|
| Lawful basis & transparency | Enrollment terms include data-processing notice; guardian consent captured at admission (document flag on student record) |
| Data minimization | Collect only fields with a documented use; unsuccessful-applicant purge (doc 07 lifecycle) |
| Purpose limitation | Health data ring-fenced (BR-HE-001/003); analytics anonymized/aggregated |
| Access & correction rights | Guardian self-view of ward data; correction requests through School Admin (audited) |
| Erasure | BR-SE-004 procedure: anonymization scripts, Head-authorized, audited; academic transcript minimums retained as legal-obligation carve-out |
| Security of processing | TLS everywhere, encrypted backups, secrets via env (never in repo), least-privilege DB accounts |
| Breach readiness | Incident log + notification procedure documented before go-live (task for implementation phase) |

**PII handling in engineering practice:** no PII in logs (NFR-17), no PII in URLs, no production data in dev environments (seed scripts generate synthetic Ghanaian-realistic data), photos/documents stored outside the DB (filesystem/object storage) with access-checked streaming endpoints.

## 5. Audit Logging Strategy

- What: every domain mutation + every approval/publication + every auth event + every export (BR-SE-002).
- How: service-layer interception writing `audit_log` in-transaction (doc 09 §6).
- Who reads: SYSTEM_ADMIN, HEAD (read-only, FR-AUD-01). Audit data itself contains PII → same protection class as source data.
- Never: audit rows edited or deleted; retention ≥ 7 years.

## 6. Secrets & Configuration

Env-var configuration only (12-factor); `.env.example` documents every key with safe defaults for local dev; production secrets live in the deployment platform's secret store. JWT secret rotation procedure documented at deployment time. Database credentials per-environment, least privilege (app user ≠ migration user is a production option — decide at deployment).

## 7. Transport & Headers

TLS termination at the reverse proxy; HSTS; `Cache-Control: no-store` on API responses containing personal data; CORS locked to the known frontend origin(s) when the frontend exists — no wildcard.
