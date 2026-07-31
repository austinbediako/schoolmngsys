# 12 — Gap Analysis

> Method: Concept Document → expected operations of a real Ghanaian basic school → required modules →
> business rules → **what's missing or ambiguous**. Each gap has a disposition: **Resolved-in-docs**
> (this documentation set already covers it, with the ruling cited), **Open-question** (school must decide;
> tracked in [task.md](../task.md)), or **Deferred** (consciously out of MVP/scope; tracked in [13 — Roadmap](13-roadmap.md)).

## 1. Gaps in the Concept Document

| ID | Gap | Why it matters | Disposition |
|---|---|---|---|
| G-01 | No academic calendar management mentioned | Terms/school days underpin attendance, results, billing | **Resolved-in-docs:** Academic Structure module, BR-AS-001..003, WF-01 |
| G-02 | Promotion/repetition rules absent | Year-end cannot run without a policy | **Resolved:** defaults confirmed (A-08, 2026-07-29) |
| G-03 | Report card content unspecified (positions, conduct, remarks) | Core parent-facing artifact | **Resolved-in-docs:** glossary definition, BR-AA-004/005, FR-RES-05; exact layout = frontend-phase decision with school |
| G-04 | Assessment weighting (SBA/exam) unstated | Results math undefined without it | **Resolved:** 30/70 confirmed (A-03, 2026-07-29) |
| G-05 | Grade scale unstated | Same | **Resolved:** default bands confirmed (A-04, 2026-07-29) |
| G-06 | Mid-year transfers (in/out) unhandled | Real occurrence every term | **Resolved-in-docs:** BR-AD-005, BR-EN-005, WF edge cases |
| G-07 | Attendance granularity (daily vs per-subject at JHS) | Data model differs | **Resolved-in-docs:** daily for MVP (BR-AT-001); per-subject deferred |
| G-08 | Fee policy details: part payment, arrears, discounts, refunds, non-payment consequences | Accountant cannot operate without them | **Resolved:** BR-FI-002..007; refunds = reversal entries; toggles confirmed (A-09/A-10, 2026-07-29) |
| G-09 | Sibling relationships / family billing view | Common in practice; affects communication consolidation | Partially resolved (shared guardians via links, WF-08 consolidation); family-level billing statements **Deferred** |
| G-10 | Discipline/conduct incident records | Schools keep these; sensitive data | **Deferred** (Roadmap) — conduct *remark* on report card covered |
| G-11 | Extra-curricular: houses, clubs, sports | Report cards sometimes reference | **Deferred** |
| G-12 | ID cards / student photos usage | Operational need | Photos **Resolved-in-docs** (student record); card generation **Deferred** |
| G-13 | Document management (birth certs, transfer letters) | Admissions requires | **Resolved-in-docs:** FR-STU-01, storage in doc 11 §4 |
| G-14 | Notification consent/opt-out & messaging cost ownership | DPA + budget reality | Opt-in prefs FR-PAR-02 (post-MVP); cost logging BR-CO-004; consent at admission (doc 11 §4) |
| G-15 | Data protection (Act 843) not mentioned at all | Legal obligation, children's data | **Resolved-in-docs:** doc 11 §4, BR-SE-* |
| G-16 | "Learning Management" scope undefined | Could mean anything from homework to full LMS | **Resolved-in-docs:** scoped to materials/assignments (FR-LMS-01); full LMS out of scope (doc 01) |
| G-17 | Timetable constraints (teacher availability, subject period counts) | Real scheduling is constraint-heavy | MVP-excluded; clash rules only (BR-TT-001); constraint solving **Deferred** |
| G-18 | Multi-guardian custody nuances (separated parents, restricted contact) | Sensitive, real | **Resolved-in-docs:** hasCustody/contact flags on links; restricted-contact handling = **Open-question** with school |
| G-19 | Staff leave, appraisal, payroll interface | HR completeness | Leave post-MVP (FR-STF-03); appraisal/payroll **Deferred** (University handles payroll) |
| G-20 | Offline operation (power/network outage during attendance) | Ghana reality | **Deferred** design concern: API is bulk-submit friendly (FR-ATT-01) so a future offline-first client can sync; no offline server mode |
| G-21 | Alumni & transcript requests after graduation | Recurring school workload | Data preserved by ADR-006/007; request workflow **Deferred** |
| G-22 | BECE school placement (SHS selection support) | JHS 3 guardians ask for it | **Deferred** — school counselling process, not system of record |
| G-23 | Guardian message consolidation (one guardian with multiple wards gets one message per event per ward, not one combined message) | Named explicitly in the WP-8 test plan (docs/14 §5); avoids notification spam for multi-child households | **Deferred** — `NotificationEventListener` (WP-8) sends per-(student, event); batching by guardian across wards is a distinct piece of work, not built in WP-8 |
| G-24 | BR-PR-001's "after Term 3 results are published" precondition isn't technically enforced — `PromotionService.initiateRun` (WP-9) will happily generate promotion decisions for a source academic year with no published results at all | BR-PR-001 is a documented rule; the auto-decision engine doesn't read scores anyway (PROMOTE/GRADUATE is purely by class-level sequence, REPEAT is always a manual exception), so this is a timing/process safeguard rather than a computational dependency, but it's currently just a convention, not a gate | **Open-question**/fast-follow — needs a small cross-module read (assessment → progression: "has this enrollment's Term 3 report card been published") before it can be enforced; not built in WP-9 |
| G-25 | A BECE module (registration, WAEC stanine import) and an admin-bootstrap/audit-query/cash-book-export bundle were built and mislabeled "WP-10"/"WP-11" (2026-07-31), even though BECE is explicitly POST-MVP (this document's own scope table, docs/05, docs/14 §2) and no WP-11 exists in the plan at all. The real WP-10 (analytics/dashboards) remains unbuilt. | Scope-creep risk: a completion claim ("Phase 1 complete, ready for Phase 2") was made and found false; two real defects shipped inside the out-of-plan work (BECE's permissions were never seeded — every endpoint 403'd unconditionally; the admin bootstrap used a hardcoded, unenforced default password) — both now fixed, but the scope question itself is unresolved | **Open-question** — project owner must decide: keep BECE/admin-bootstrap/audit/export as ratified extra-Phase-1 scope, or roll them back to stay strictly inside the approved boundary; either way, WP-10 (analytics) still needs to be built to actually close M5 |

## 2. Ambiguities Resolved by Documented Assumption

All **A-nn** assumptions are indexed in [04 — Business Rules, Assumptions Index](04-business-rules.md#assumptions-index) with their owners. **All eleven were confirmed by their owners on 2026-07-29** — the documented defaults are now binding starting values (per-year configurable where so marked).

## 3. Risks Raised by This Analysis

| Risk | Mitigation |
|---|---|
| ~~School stakeholders unavailable to confirm assumptions~~ | **Closed 2026-07-29** — all A-nn confirmed; values remain per-year configurable regardless |
| Concept modules (17) tempt scope creep before MVP proves value | Feature matrix phases everything; MVP boundary is explicit (doc 01 §5) |
| SMS costs surprise the school | BR-CO-004 delivery/cost log from day one; consolidation logic in WF-08 |
| Single developer/small team vs 17 modules | Modular monolith + strict MVP; post-MVP modules are additive, not architectural |
