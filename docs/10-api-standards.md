# 10 — API Standards & Planning

> API-first: these conventions bind all endpoints (see also `.claude/rules/Api.md`: every endpoint validates input;
> errors use the standard format). This is planning documentation — no endpoint is implemented by this file.

## 1. General Conventions

| Concern | Standard |
|---|---|
| Style | REST over HTTPS, JSON bodies (UTF-8) |
| Base path & versioning | `/api/v1/...` — URI-versioned; v1 is frozen at first frontend integration; additive changes allowed, breaking changes require v2 |
| Resource naming | plural kebab-case nouns: `/students`, `/academic-years`, `/term-results`; nesting max one level deep, then flatten with query filters |
| JSON fields | `camelCase`; dates ISO-8601 (`2027-01-09` / `2027-01-09T08:00:00Z`); money as decimal string `"1250.00"` with implicit GHS |
| IDs | UUID strings (doc 09 §2); business numbers (studentNumber, receiptNumber) are display fields, never path IDs |
| Pagination | `?page=0&size=20` (size cap 100) → response envelope `{content, page, size, totalElements, totalPages}` |
| Sorting/filtering | `?sort=lastName,asc` · documented filter params per collection; no generic query language |
| Idempotency | Mutating financial/bulk endpoints accept `Idempotency-Key` header (payment capture, billing run, promotion run) |
| Concurrency | Optimistic: entities expose `version`; updates send `If-Match`/version and get `409` on conflict |
| Async operations | Long jobs (billing run, promotion run, result computation) return `202` + job resource `/jobs/{id}` to poll |

## 2. Error Format (RFC 7807)

Canonical definition of the project's error contract; [CLAUDE.md](../CLAUDE.md) and [.claude/rules/Api.md](../.claude/rules/Api.md) mandate it and point here.

`application/problem+json`; extensions: `errors[]` for field violations, `traceId` for correlation.

```json
{
  "type": "https://ubs-lmis.example/problems/validation",
  "title": "Validation failed",
  "status": 400,
  "detail": "2 fields are invalid",
  "instance": "/api/v1/students",
  "traceId": "…",
  "errors": [
    {"field": "dateOfBirth", "message": "must be in the past"}
  ]
}
```

Problem catalog (stable `type` slugs): `validation`, `not-found`, `conflict` (state/version), `rule-violation` (business rule, include `ruleId` e.g. `BR-EN-001`), `auth-required`, `forbidden`, `rate-limited`, `internal`. Business-rule rejections cite the rule ID — errors become self-documenting against [04 — Business Rules](04-business-rules.md).

## 3. Validation Strategy

- **Syntactic validation** (shape, format, ranges) at the DTO boundary via Bean Validation — every request DTO annotated; controllers never accept unvalidated input (`.claude/rules/Api.md`).
- **Semantic validation** (business rules) in services, throwing rule-violation problems with `ruleId`.
- **Persistence constraints** (doc 09 §4) as the final net — constraint violations map to `conflict`, never leak SQL.
- Normalize on input: phone numbers to E.164 (+233…), names trimmed, enum values case-insensitive in, canonical out.

## 4. Security Conventions (details in doc 11)

- `Authorization: Bearer <JWT>`; refresh via `/api/v1/auth/refresh` (rotating refresh tokens).
- Endpoint authorization = permission check + **scope filter** (own-classes / own-wards / department) applied server-side; scope rules per [03 §3](03-roles-and-permissions.md).
- No student PII in URLs (IDs only); responses expose the minimum fields the permission grants (e.g. Librarian sees identity-only student view).

## 5. Resource Map (planning-level, per module)

Only shapes and ownership — parameters/payloads are designed per-feature at implementation time against these rules.

| Module | Primary resources (under `/api/v1`) |
|---|---|
| auth | `/auth/login`, `/auth/refresh`, `/auth/logout`, `/accounts`, `/roles` |
| academics | `/academic-years`, `/academic-years/{id}/terms`, `/classes`, `/subjects`, `/classes/{id}/subject-offerings`, `/school-days` (query) |
| people | `/students`, `/guardians`, `/students/{id}/guardians`, `/staff`, `/students/{id}/documents` |
| enrollment | `/enrollments`, `/classes/{id}/roster`, (post-MVP) `/applications` |
| attendance | `/attendance/registers` (bulk day capture), `/attendance/records`, `/attendance/summaries` |
| assessment | `/assessment-components`, `/scores` (bulk), `/term-results`, `/report-cards`, `/grade-scales`, result pipeline actions as sub-resources (`/term-results/submit`, `/approve`, `/publish`) |
| progression | `/promotion-runs`, `/promotion-decisions`, (post-MVP) `/bece/candidates`, `/bece/results-import` |
| finance | `/fee-schedules`, `/invoices`, `/payments`, `/adjustments`, `/finance/reports/*` |
| communication | `/announcements`, `/notifications` (own inbox), `/message-templates` |
| analytics | `/dashboards/head`, `/dashboards/teacher`, … |
| audit | `/audit-log` (query, restricted) |
| jobs | `/jobs/{id}` (async job status) |

**Action-style endpoints** (verbs) are allowed only for genuine workflow transitions (`…/publish`, `…/reverse`), never as CRUD substitutes.

## 6. OpenAPI & Documentation

Spec generated from code annotations, published at `/api-docs`; CI check keeps it building. The spec documents *what exists*; this file governs *how things must look*. Divergence = review blocker.
