# ADR-009 — Headless Backend Now; Web Frontend as Separate Application Later

**Status:** Accepted · 2026-07-28

## Problem
Sequence backend and frontend work; decide whether the backend serves UI.

## Context
Concept document defers the frontend. Stakeholder direction: build the system (backend) first; a Next.js frontend is the working plan for later. Server-side rendering from Spring (Thymeleaf) would couple UI cadence to backend releases and duplicate effort once the real frontend lands.

## Decision
The Spring Boot app is purely an API + workflow engine (Product Principle #5: server-side truth). No server-rendered UI, no static assets. The API is designed against [10 — API Standards](../10-api-standards.md) so any client (web, mobile) can consume it. Frontend-facing NFRs (18–20) constrain API design now (complete data, pagination, low-bandwidth) so the later frontend needs no backend rework.

## Trade-offs
(+) Clean separation, parallel workstreams later, mobile-ready.
(−) No UI for early demos — mitigated by OpenAPI/It-tools style exploration and seeded data; admin operations possible via API tooling until UI exists.

## Future Implications
CORS/authentication flows (doc 11 §7) activate when the frontend origin is known. A BFF layer remains possible (ADR-003).
