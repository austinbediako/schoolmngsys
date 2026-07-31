# CONTEXT.md — Session Bootstrap & Knowledge Protocol

> **Start here.** This document exists so that any engineer or AI session can reconstruct full understanding
> of UBS-LMIS from the repository alone, with no access to prior conversations.
>
> **This file owns:** the loading protocol, the knowledge-ownership registry, and the rules for maintaining
> the repository as a knowledge library. It does **not** restate project content — it tells you where content lives.
> Rationale for this whole approach: [ADR-011](docs/adr/ADR-011-documentation-driven-repository.md).

## 1. Loading Protocol

When told *"Read CONTEXT.md"* or *"Load project context"*, read in tiers. Do not stop after one file.

### Tier 0 — Always (orientation, ~5 minutes)

| Order | File | What you get |
|---|---|---|
| 1 | **CONTEXT.md** (this file) | How the repository is organized and maintained |
| 2 | [CLAUDE.md](CLAUDE.md) | Engineering constitution: standards, conventions, decision rules, Definition of Done |
| 3 | [memory.md](memory.md) | Long-term project knowledge: non-negotiables, domain facts, decisions log |
| 4 | [task.md](task.md) | **Authoritative current state**: milestone, pending work, blockers |

After Tier 0 you know *what the project is, how we work, and what is happening right now*.

### Tier 1 — Domain understanding (before any substantive work)

| Order | File | What you get |
|---|---|---|
| 5 | [UBS-LMIS_Concept_Document.md](UBS-LMIS_Concept_Document.md) | Original stakeholder input (the source everything traces to) |
| 6 | [docs/glossary.md](docs/glossary.md) | Canonical domain language — read before the rest; the other docs assume these terms |
| 7 | [docs/01-product-vision.md](docs/01-product-vision.md) | Vision, users, scope boundary, MVP definition |
| 8 | [docs/02-domain-model.md](docs/02-domain-model.md) | Bounded contexts, entities, relationships, domain events |
| 9 | [docs/04-business-rules.md](docs/04-business-rules.md) | The binding rule registry (BR-…) and confirmed assumptions (A-…) |

### Tier 2 — Task-directed (read what your work touches)

| If you are working on… | Read |
|---|---|
| Any code at all | [docs/08 — Module Architecture](docs/08-module-architecture.md), [docs/14 — Implementation Plan](docs/14-implementation-plan.md) |
| An API endpoint | [docs/10 — API Standards](docs/10-api-standards.md), [.claude/rules/Api.md](.claude/rules/Api.md) |
| Entities or migrations | [docs/09 — Data Architecture](docs/09-data-architecture.md), ADR-005, ADR-006 |
| Auth, permissions, access | [docs/03 — Roles & Permissions](docs/03-roles-and-permissions.md), [docs/11 — Security & Privacy](docs/11-security-and-privacy.md), ADR-004 |
| A feature's behavior | [docs/05 — Functional Requirements](docs/05-functional-requirements.md), [docs/07 — Workflows](docs/07-workflows.md) |
| Performance, ops, deployment | [docs/06 — NFRs](docs/06-non-functional-requirements.md), [docs/13 — Roadmap](docs/13-roadmap.md) |
| Understanding *why* something is the way it is | [docs/adr/](docs/adr/) — all ADRs |
| Wondering what's missing or unresolved | [docs/12 — Gap Analysis](docs/12-gap-analysis.md) |

Full catalog with one-line descriptions: [docs/README.md](docs/README.md).

## 2. Knowledge Ownership Registry

**One topic, one canonical home.** Other documents reference by ID — never restate. If you find the same fact
explained in two places, that is a defect: consolidate to the owner and replace the copy with a reference.

| Knowledge domain | Canonical owner | Identifier scheme |
|---|---|---|
| **Terminology** | [docs/glossary.md](docs/glossary.md) | — |
| **Product** — vision, scope, MVP boundary, principles | [docs/01](docs/01-product-vision.md) | — |
| **Business domain** — contexts, entities, events | [docs/02](docs/02-domain-model.md) | — |
| **Access model** — roles, permissions, scopes | [docs/03](docs/03-roles-and-permissions.md) (matrix) · [docs/14 §6](docs/14-implementation-plan.md#6-permission-catalog) (catalog) | permission strings |
| **Business rules & assumptions** | [docs/04](docs/04-business-rules.md) | `BR-XX-nnn`, `A-nn` |
| **Requirements** — functional | [docs/05](docs/05-functional-requirements.md) | `FR-MOD-nn` |
| **Requirements** — non-functional | [docs/06](docs/06-non-functional-requirements.md) | `NFR-nn` |
| **Workflows & data lifecycle** | [docs/07](docs/07-workflows.md) | `WF-nn` |
| **Architecture** — modules, boundaries, dependencies | [docs/08](docs/08-module-architecture.md) | — |
| **Database** — standards, naming, integrity, migrations | [docs/09](docs/09-data-architecture.md) | migration `V<n>__…` |
| **API** — conventions, errors, validation, resources | [docs/10](docs/10-api-standards.md) | — |
| **Security & privacy** — threat model, authz, DPA | [docs/11](docs/11-security-and-privacy.md) | — |
| **Gaps & open questions** | [docs/12](docs/12-gap-analysis.md) | `G-nn` |
| **Roadmap & scaling** | [docs/13](docs/13-roadmap.md) | phases |
| **Delivery plan** — work packages, milestones, tests | [docs/14](docs/14-implementation-plan.md) | `WP-n`, `M-n` |
| **Decisions & their rationale** | [docs/adr/](docs/adr/) | `ADR-nnn` |
| **Engineering standards & practice** | [CLAUDE.md](CLAUDE.md) | — |
| **Long-term project knowledge** | [memory.md](memory.md) | — |
| **Current status & next steps** | [task.md](task.md) | — |
| **Knowledge protocol & library organization** | **CONTEXT.md** (this file) | — |

### Where does new knowledge go?

| You learned… | Put it in |
|---|---|
| A rule the school enforces | [docs/04](docs/04-business-rules.md) as a new `BR-`; cite it wherever it applies |
| Why we chose an approach | A new ADR in [docs/adr/](docs/adr/) (Problem · Context · Options · Decision · Trade-offs · Future Implications) |
| A durable fact about the domain or project | [memory.md](memory.md) |
| What we're doing right now / next | [task.md](task.md) |
| A new term | [docs/glossary.md](docs/glossary.md) **first**, then use it consistently |
| Something unclear that you had to assume | [docs/04 assumptions index](docs/04-business-rules.md#assumptions-index) as `A-nn` with an owner — **never assume silently** |
| Something missing from the concept document | [docs/12](docs/12-gap-analysis.md) as `G-nn` with a disposition |
| A change to how we build | [CLAUDE.md](CLAUDE.md) |

## 3. Maintenance Rules

### Continuous documentation
Every task includes asking *"what documentation must change with this?"* — and doing it **in the same change**,
never as a follow-up. Architecture changes update [docs/08](docs/08-module-architecture.md) + an ADR. Rule changes
update [docs/04](docs/04-business-rules.md). Practice changes update [CLAUDE.md](CLAUDE.md). Knowledge growth updates
[memory.md](memory.md). Status changes update [task.md](task.md).

### Assumptions become decisions
When an assumption is confirmed, update its index row with the confirmation and date, and resolve every gap row
that depended on it. Keep the original `[ASSUMPTION A-nn]` marker in the rule text — it preserves the trail showing
which rules originated as inference rather than from stakeholders.

### Self-healing sweeps
While working, watch for and fix: contradictions, stale statements, duplicated knowledge, inconsistent terminology,
broken references, requirements with no architectural home, and architecture with no requirement behind it.
Findings get fixed or recorded — never silently ignored.

**Reference integrity** is mechanically checkable and should be verified after any change that adds links or renames
headings: for every `[text](file#anchor)`, confirm the file exists and the anchor matches a heading slug
(lowercase, punctuation dropped, spaces → hyphens). **Known pitfall:** headings containing `—`, `×`, or similar symbols
slug inconsistently across renderers (the symbol vanishes but its surrounding spaces each become a hyphen, yielding
`--`). When adding a link target, prefer a heading without such symbols, or simplify the heading rather than encoding a
renderer quirk into the link.

### Library growth policy
`docs/` is currently a flat numbered sequence plus `adr/`, which stays discoverable at this size and encodes reading
order in the filenames. **Do not pre-create folders.** When one knowledge domain grows past roughly four or five
documents, it graduates into a subfolder with its own index, and [docs/README.md](docs/README.md) plus this registry
are updated in the same change. Structure follows actual content, not anticipated content.

## 4. End-of-Session Checklist

Before concluding a session, verify every line:

- [ ] The requested task is complete (or its precise stopping point is recorded in [task.md](task.md))
- [ ] Documentation reflects the current state of the system
- [ ] [memory.md](memory.md) captures new long-term knowledge and any decisions made
- [ ] [task.md](task.md) reflects current milestone, pending work, and blockers
- [ ] [CLAUDE.md](CLAUDE.md) still matches how we actually work
- [ ] Architecture docs and ADRs reflect decisions actually taken
- [ ] New business rules are documented with IDs and cited where they apply
- [ ] Assumptions made this session are documented (`A-nn`) or resolved
- [ ] Cross-references and anchors still resolve
- [ ] **The repository is easier to understand than it was at the start of the session**

## 5. Current State

Phase 0 (engineering foundation) is complete. Phase 1 (MVP backend) is in progress: WP-0 through
WP-9 are complete — M1 (walking skeleton), M2 (registry ready), M3 (daily operations), and M4
(term close) are all closed; M5 is in progress. **WP-10 (analytics) is next and has not been
started** — a prior session's claim that WP-10/WP-11 were done and Phase 1 was complete was
verified false on 2026-07-31: what was actually built under those labels was the BECE module
(explicitly out of Phase 1 scope) and an admin-bootstrap/audit/export bundle (not an official work
package), both containing real defects since fixed — see task.md and memory.md's "Post-agent
verification sweep" for the full account. **[task.md](task.md) is the authority on status** — if
this section and task.md ever disagree, task.md wins and this line should be corrected.
