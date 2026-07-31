# ADR-011 — Documentation-Driven Repository with an Explicit Context Protocol

**Status:** Accepted · 2026-07-29

## Problem
Project understanding for UBS-LMIS is being built largely through AI-assisted sessions whose conversation context is
temporary. Without a deliberate mechanism, the reasoning behind the system would live in chat histories that no future
engineer or session can read — leaving a repository whose *what* is visible but whose *why* is lost.

## Context
- The domain carries a large volume of non-obvious rules (GES/NaCCA/WAEC structures, assessment weighting, promotion policy, fee allocation) that are expensive to re-derive and dangerous to guess at.
- Eleven documented assumptions were confirmed by stakeholders on 2026-07-29; that confirmation is worthless if it isn't durably recorded against the rules it validates.
- Contributors will be intermittent — a small team plus AI sessions that always start cold.
- Documentation already exists as a full engineering set (`docs/` 01–14, ADR-001…010); what was missing was a **protocol** for loading, owning, and maintaining it.

## Options Considered
1. **Rely on conversation continuity and code comments.** Zero upfront cost; guarantees knowledge loss at every session boundary and cannot carry rules that have no single code home (policy, rationale, rejected alternatives).
2. **Documentation as a deliverable produced at milestones.** Better, but documentation written after the fact drifts from reality and is treated as optional under delivery pressure.
3. **Documentation-driven repository with a defined context protocol** (chosen): a single entry point (`CONTEXT.md`) defining tiered reading order, a knowledge-ownership registry enforcing one canonical home per topic, in-change documentation updates, and an end-of-session checklist.

## Decision
Option 3. `CONTEXT.md` owns the loading protocol, ownership registry, and maintenance rules. Documentation updates are
part of the change that necessitates them, not follow-up work — enforced through the review checklist and Definition of
Done in [CLAUDE.md](../../CLAUDE.md). Knowledge that would otherwise be conversational (rules, rationale, assumptions,
gaps) is persisted under an ID scheme so it can be cited rather than restated.

## Trade-offs
**(+)** Any cold start — human or AI — reaches full understanding from the repository alone. Rationale survives staff and
session turnover. Duplication is structurally discouraged by the ownership registry, so contradictions are rarer and
cheaper to fix. Confirmed decisions stop being re-litigated.

**(−)** Real ongoing cost: every substantive change carries a documentation obligation, and stale documentation is
actively misleading rather than merely absent. Mitigations: one canonical home per topic (so an update touches one
place), IDs instead of copied prose, and mechanical link/anchor verification. Discipline is enforced by review, not by
tooling — the residual risk is accepted at this team size.

## Future Implications
- The ID schemes (`BR-`, `FR-`, `NFR-`, `WF-`, `G-`, `A-`, `ADR-`, `WP-`) become the vocabulary of commits, PRs, and API
  error payloads (business-rule rejections cite `ruleId` per [docs/10 §2](../10-api-standards.md#2-error-format-rfc-7807)), making the documentation executable in practice rather than decorative.
- If the documentation set outgrows a flat structure, the growth policy in `CONTEXT.md` §3 governs reorganization — structure follows content.
- Should this repository ever gain a frontend or second service, the protocol extends by adding tiers and registry rows, not by forking the approach.
