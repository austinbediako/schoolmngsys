# ADR-007 — Append-Only Audit Log + Soft Delete by Default

**Status:** Accepted · 2026-07-28

## Problem
Accountability for changes to children's academic and financial records; safe deletion semantics.

## Context
Grade tampering and financial fraud are the top insider threats (doc 11 §1). Ghana DPA grants erasure rights, but transcripts and financial law require retention — deletion cannot be a plain `DELETE`.

## Decision
- Single append-only `audit_log` written in-transaction via service-layer interception (doc 09 §6); covers all domain mutations, approvals, auth events, exports (BR-SE-002).
- Soft delete (`archived_at`) as the only user-facing deletion (BR-SE-004); hard erasure only via documented DPA anonymization procedures, Head-authorized, audited.
- Financial corrections are compensating entries, never edits (BR-FI-003); published results correct via revisions (BR-AA-006).

## Trade-offs
(+) Every dispute answerable; DPA erasure and legal retention reconciled.
(−) Audit volume (partition-ready design, doc 09 §6); queries must filter `archived_at` (mitigate: repository defaults + partial indexes).

## Future Implications
The audit stream is a ready-made input for anomaly alerts (e.g. after-hours grade edits) — Roadmap candidate.
