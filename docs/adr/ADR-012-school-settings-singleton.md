# ADR-012 — School Settings as a Singleton Module

**Status:** Accepted · 2026-08-01

## Problem
A WP-1..WP-10 backend audit found no home for basic school-wide configuration (name, motto, address, contact info, logo, notification toggles) — no entity, no endpoint.

## Context
Academic settings (grade weighting/bands), attendance settings (school-day calendar), and fee settings (fee schedules) already have real owning homes per academic year/term — duplicating them into a generic "settings" bag would create a second source of truth. What's genuinely missing is school-identity/branding/contact data and a couple of system-wide notification toggles, which don't belong to any academic year and have no other owner.

## Options
1. Add fields to an existing module (e.g. `academics` or `shared`) — wrong ownership, couples an unrelated concern to that module's lifecycle.
2. A generic key-value settings table — flexible but untyped, no validation, invites scope creep (every future "setting" gets dumped in with no review).
3. **A dedicated `school` module owning exactly one typed `SchoolSettings` row**, enforced singleton via a partial unique index on a constant expression (`school_settings ((true)) WHERE archived_at IS NULL`) — the same "at most one" technique already used elsewhere in this schema.

## Decision
Option 3. `school` depends on nothing (not even `people`/`academics`) and nothing depends on it — a genuinely leaf, standalone module. `SchoolSettingsService` only ever reads/updates the one seeded row (V19); the unique index makes creating a second row impossible even by mistake.

## Trade-offs
(+) Typed, validated fields; clear single ownership; zero coupling to other modules; extending it later (new field) is a normal migration, not a schema redesign.
(−) One more module for a small amount of data — acceptable given the alternative (bolting it onto an unrelated module) is worse for long-term maintainability.

## Future Implications
If genuine per-year "school configuration" needs emerge (e.g. a setting that legitimately varies by academic year), that's a different entity scoped to `AcademicYear`, not an extension of this singleton — don't retrofit year-scoping onto `SchoolSettings`.
