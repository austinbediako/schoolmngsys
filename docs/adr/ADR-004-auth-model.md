# ADR-004 — JWT + Rotating Refresh, RBAC with Permissions, Account≠Person

**Status:** Accepted · 2026-07-28

## Problem
Authentication and authorization model for ten user types with scope-bound access to children's data.

## Context
JJWT already a dependency; stateless API desired; but immediate revocation matters (staff exits, compromised guardian accounts — BR-ST-002, FR-AUTH-04). Roles overlap (teacher-parent), and role names must not leak into endpoint logic or new roles force code churn.

## Options considered
- Sessions (server state everywhere) vs pure stateless JWT (no revocation) vs **JWT access + persisted rotating refresh** (chosen middle).
- Role-name checks in code vs **permission-string checks with roles as bundles** (chosen).
- User table doubling as person registry vs **UserAccount linked 0..1 to Person** (chosen — Nursery students are people without logins; a person's roles can change without touching identity).

## Decision
Short-TTL JWT access tokens; hashed rotating refresh tokens in DB; permissions per [03 §4](../03-roles-and-permissions.md); scope filters in service layer (doc 11 §3); accounts provisioned only by workflows (BR-SE-003).

## Trade-offs
(+) Revocation ≤ access TTL; role composition without code change; clean person modeling.
(−) Refresh persistence = state (accepted); permission list must stay synced with matrix (review-checklist item).

## Future Implications
HS256 now; move to asymmetric signing when a second token consumer exists. SSO (University of Ghana staff directory) is a Roadmap candidate — the account/person split keeps it non-disruptive.
