# ADR-008 — Event-Driven Notifications via Transactional Outbox, SMS-First

**Status:** Accepted · 2026-07-28

## Problem
Deliver guardian/staff notifications reliably without coupling domain workflows to messaging providers.

## Context
SMS is the dependable guardian channel in Ghana (BR-CO-001); providers (Hubtel, Arkesel, Twilio et al.) have outages and per-message cost. Publishing results must never fail because an SMS gateway is down (NFR-08).

## Options
1. Synchronous provider calls inside workflows. 2. Fire-and-forget async calls (loss on crash). 3. **Transactional outbox**: domain events written to `notification_outbox` in the same transaction; a dispatcher delivers with retry/backoff and records delivery status.

## Decision
Option 3. Channel-agnostic templates; provider behind an adapter interface chosen per channel; every send logged (BR-CO-004). Provider selection itself is a deployment-time decision, deliberately deferred — the adapter seam is the architectural commitment.

## Trade-offs
(+) Reliability, cost auditability, provider swap without domain change, consolidated-message logic in one place (WF-08).
(−) Slight delivery latency (dispatcher cadence — acceptable); one more moving part (in-process scheduler initially, not external infra).

## Future Implications
The same outbox carries email/in-app/push later; two-way SMS or WhatsApp (Roadmap) plug in as new adapters.
