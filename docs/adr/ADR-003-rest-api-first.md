# ADR-003 — API-First REST/JSON with RFC 7807 Errors

**Status:** Accepted · 2026-07-28

## Problem
Define the contract style between the headless backend and its future clients (web frontend, possibly mobile).

## Context
Frontend arrives later (Next.js planned); the API is the product surface until then. Project convention (CLAUDE.md, `.claude/rules/Api.md`) already mandates RFC 7807 and universal input validation. Team familiarity and tooling (OpenAPI, Spring MVC) favor REST.

## Options
1. REST/JSON (URI-versioned). 2. GraphQL. 3. gRPC.

## Decision
REST/JSON under `/api/v1`, standards in [10 — API Standards](../10-api-standards.md); OpenAPI generated from code; RFC 7807 problem details with business-rule IDs.

## Trade-offs
(+) Ubiquitous tooling, cacheability, simple auth integration, easy manual testing.
(−) Some over/under-fetching vs GraphQL — mitigated by purpose-built read endpoints (dashboards, report-card data) rather than generic graphs.

## Future Implications
A BFF or GraphQL layer can be added in front later without changing the system of record's API.
