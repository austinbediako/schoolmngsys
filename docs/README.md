# UBS-LMIS Engineering Documentation

Catalog of the `docs/` set for the University Basic School Legon Integrated Management Information System.
Each document owns its topic — cross-reference, don't duplicate.

> **Loading project context for a new session?** Start at [../CONTEXT.md](../CONTEXT.md) instead — it defines the
> tiered reading order across the whole repository and the knowledge-ownership registry. This file is the `docs/` catalog.

## Reading Order for New Engineers

1. [01 — Product & System Vision](01-product-vision.md) — what we're building and the MVP boundary
2. [Glossary](glossary.md) — canonical domain language (read before anything else if terms confuse)
3. [02 — Domain Model](02-domain-model.md) — bounded contexts, entities, events, invariants
4. [04 — Business Rules](04-business-rules.md) — the rule registry (BR-…) + assumptions index
5. [08 — Module Architecture](08-module-architecture.md) — how the codebase is organized
6. Everything else as needed.

## Document Map

| Doc | Owns |
|---|---|
| [glossary.md](glossary.md) | Terminology (canonical) |
| [01-product-vision.md](01-product-vision.md) | Vision, users, scope, MVP boundary, product principles |
| [02-domain-model.md](02-domain-model.md) | Bounded contexts, entity catalog, relationships, domain events |
| [03-roles-and-permissions.md](03-roles-and-permissions.md) | Roles, permission matrix, scopes, account lifecycle |
| [04-business-rules.md](04-business-rules.md) | Business rules registry (BR-…), assumptions (A-…) |
| [05-functional-requirements.md](05-functional-requirements.md) | Requirements per module (FR-…), feature matrix |
| [06-non-functional-requirements.md](06-non-functional-requirements.md) | NFR targets |
| [07-workflows.md](07-workflows.md) | End-to-end workflows (WF-…), data lifecycle |
| [08-module-architecture.md](08-module-architecture.md) | Package layout, boundary rules, dependency map, module specs |
| [09-data-architecture.md](09-data-architecture.md) | Persistence standards, naming, integrity, migrations discipline |
| [10-api-standards.md](10-api-standards.md) | API conventions, error format, validation strategy, resource map |
| [11-security-and-privacy.md](11-security-and-privacy.md) | Threat model, authn/z design, DPA compliance, audit strategy |
| [12-gap-analysis.md](12-gap-analysis.md) | Concept-vs-reality gaps (G-…) and dispositions |
| [13-roadmap.md](13-roadmap.md) | Phases, scaling triggers, deployment evolution |
| [14-implementation-plan.md](14-implementation-plan.md) | Phase 1 work packages, milestones, migration sequence, permission catalog, test plan |
| [adr/](adr/) | Architecture Decision Records (ADR-001…011) |

## Related Root Files

- [../CONTEXT.md](../CONTEXT.md) — session bootstrap protocol, knowledge-ownership registry, maintenance rules
- [../CLAUDE.md](../CLAUDE.md) — engineering constitution (conventions, standards, Definition of Done)
- [../memory.md](../memory.md) — persistent project memory (decisions, constraints, domain knowledge)
- [../task.md](../task.md) — current milestone, open questions, next steps
- [../UBS-LMIS_Concept_Document.md](../UBS-LMIS_Concept_Document.md) — original concept input

## Maintenance Rules

Canonical rules live in [../CONTEXT.md §3](../CONTEXT.md#3-maintenance-rules). In short: terminology changes start in the
glossary, rule changes in doc 04, architectural changes with an ADR; documents cite IDs rather than restating content;
duplication is a defect to consolidate.
