# 04 — Business Rules Catalog

> **Canonical rule registry.** Every rule has a stable ID; other documents cite IDs instead of restating rules.
> Rules marked **[ASSUMPTION A-nn]** were not in the concept document; all eleven were **confirmed by school
> stakeholders on 2026-07-29** (see index at the bottom) — the markers remain for traceability to their origin.
> "Configurable" means per-academic-year configuration, changeable only by `HEAD_OF_SCHOOL`/`SYSTEM_ADMIN` with audit.

## Academic Structure (BR-AS)

| ID | Rule |
|---|---|
| BR-AS-001 | An Academic Year contains exactly three Terms. Exactly one Academic Year is ACTIVE at any time. |
| BR-AS-002 | Class levels form a fixed ordered ladder: N1 → N2 → KG1 → KG2 → B1…B6 (Primary 1–6) → B7…B9 (JHS 1–3). The ladder is reference data, not user-editable. |
| BR-AS-003 | Term dates may be overridden per set of class levels (Term Calendar Variant). JHS 3 Terms 2–3 end earlier than the school-wide dates (from concept document). |
| BR-AS-004 | A Class belongs to exactly one Class Level; streams at the same level are distinct Classes (Primary 3A ≠ 3B). |
| BR-AS-005 | Each Class has exactly one Class Teacher per Academic Year. A teacher may be Class Teacher of at most one Class per year. **[ASSUMPTION A-01]** |
| BR-AS-006 | Subjects are offered per (Class, Year) from the NaCCA list applicable to that level; a subject offering has exactly one assigned teacher at a time. |
| BR-AS-007 | Closing an Academic Year is irreversible and requires: all term results published or explicitly voided, promotion decisions finalized, and Head approval. |

## Admissions (BR-AD)

| ID | Rule |
|---|---|
| BR-AD-001 | An application requires: applicant bio-data, date of birth evidence (birth certificate), at least one guardian with phone number, and target class level. |
| BR-AD-002 | Age guidance at entry (per GES norms): KG1 ≥ 4 years, Primary 1 ≥ 6 years by school entry. Deviations allowed with Head approval + recorded reason. **[ASSUMPTION A-02]** |
| BR-AD-003 | An admission offer converts to a Student + Enrollment only after acceptance and (configurable) initial fee payment. **[ASSUMPTION A-06]** |
| BR-AD-004 | Enrollment into a Class must not exceed class capacity unless overridden by Head with reason. |
| BR-AD-005 | Transfer-in students may join mid-year; their term results for elapsed terms are marked N/A, never fabricated. |

## Enrollment & People (BR-EN)

| ID | Rule |
|---|---|
| BR-EN-001 | A Student has at most one ACTIVE Enrollment per Academic Year. |
| BR-EN-002 | Student Number is immutable, unique, assigned at admission. Format `UBS-<entryYear>-<sequence>` **[ASSUMPTION A-05]**. |
| BR-EN-003 | Enrollments are never deleted; status transitions only (ACTIVE → COMPLETED / TRANSFERRED / WITHDRAWN). |
| BR-EN-004 | Every ACTIVE Student must have ≥ 1 linked Guardian, of whom ≥ 1 is flagged primary contact with a valid phone number. |
| BR-EN-005 | Student status GRADUATED is set only by the graduation workflow; TRANSFERRED_OUT/WITHDRAWN require a recorded reason and date. |

## Attendance (BR-AT)

| ID | Rule |
|---|---|
| BR-AT-001 | One attendance record per Student per School Day (whole-day model for MVP; per-subject JHS attendance is a post-MVP option — see Gap G-07). |
| BR-AT-002 | Attendance may only be recorded for dates that are School Days of the student's term calendar (variant-aware). |
| BR-AT-003 | Attendance statuses: PRESENT, ABSENT, LATE, EXCUSED. LATE counts as present for aggregate reporting; EXCUSED counts as absent-with-reason. **[ASSUMPTION A-07]** |
| BR-AT-004 | The Class Teacher marks attendance; edits after the school day (D+1 onward) require elevated permission and are audited with reason. |
| BR-AT-005 | Report cards show "Present X out of Y school days" computed from records, never hand-entered. |

## Assessment & Results (BR-AA)

| ID | Rule |
|---|---|
| BR-AA-001 | Term Result total = SBA weight + Exam weight. Default weighting 30% SBA / 70% Exam, configurable per Academic Year. **[ASSUMPTION A-03]** |
| BR-AA-002 | Raw scores are bounded 0..maxScore of their component; totals normalize to 0–100 with 1-decimal precision. |
| BR-AA-003 | Result lifecycle: DRAFT (teacher) → SUBMITTED (teacher locks) → HOD_APPROVED → PUBLISHED (Head). Guardians/students see only PUBLISHED. |
| BR-AA-004 | Class Position ranks students in a Class by average of subject weighted totals; equal averages share the same position (competition ranking: 1,2,2,4). |
| BR-AA-005 | Grade Scale is configurable per year; internal default **[ASSUMPTION A-04]**: 80–100 A/Excellent, 70–79 B/Very Good, 60–69 C/Good, 50–59 D/Credit, 40–49 E/Pass, 0–39 F/Fail. BECE stanine 1–9 is stored as imported, never recomputed. |
| BR-AA-006 | A PUBLISHED result is immutable. Corrections create a revision (new version + reason + approver); the report card regenerates and guardians are re-notified. |
| BR-AA-007 | A student with no score for a component is flagged, not defaulted to zero; publishing a class requires resolving all flags (score, exemption, or N/A with reason). An exempted/N/A component is excluded from that student's category total, which is then rescaled to a 0–100 basis over the remaining scored components' weights. **[ASSUMPTION A-12]** |
| BR-AA-008 | Mock exams (JHS 3) form a separate exam series and never contribute to term result weighting. |

## Promotion & Graduation (BR-PR)

| ID | Rule |
|---|---|
| BR-PR-001 | Promotion decisions occur once per year, after Term 3 results are published (mid-year admissions aside). |
| BR-PR-002 | Default decision is PROMOTE (GES norm of automatic progression). REPEAT is exceptional: proposed by Class Teacher/HoD, justified in writing, approved by Head, guardian informed. **[ASSUMPTION A-08]** |
| BR-PR-003 | Promotion moves a student exactly one level up the ladder; skipping levels requires Head approval with reason. |
| BR-PR-004 | JHS 3 students exit via graduation, not promotion. Graduation requires completed JHS 3 enrollment; BECE results attach when imported. |
| BR-PR-005 | Promotion decisions generate next year's enrollments in bulk; stream (A/B) allocation is reviewable by School Admin before confirmation. |

## BECE (BR-BE)

| ID | Rule |
|---|---|
| BR-BE-001 | Only students with an ACTIVE JHS 3 enrollment in the exam year can be BECE candidates. |
| BR-BE-002 | Candidate registration data (names, DOB, photo, subjects) is snapshotted at registration; later bio-data edits do not silently alter the snapshot. |
| BR-BE-003 | BECE results are imported per candidate per subject as WAEC stanines (1–9); imports are idempotent and audited. |

## Finance (BR-FI)

| ID | Rule |
|---|---|
| BR-FI-001 | Fee Schedules are defined per (Class Level, Term, Academic Year) and approved by the Head before invoicing. |
| BR-FI-002 | Part payments are allowed. Payments allocate to the student's invoices oldest-first unless the Accountant overrides with reason. **[ASSUMPTION A-09]** |
| BR-FI-003 | Receipts and posted payments are immutable. Errors are corrected by reversal entries, never edits or deletes. |
| BR-FI-004 | Student-specific price changes are explicit Adjustments (discount/scholarship/waiver) with reason and Head approval — never edits to the schedule or invoice lines. |
| BR-FI-005 | Unpaid balances carry forward as arrears and appear on the next term's invoice. |
| BR-FI-006 | Guardians see only their wards' invoices/payments; flagged `receivesBilling` guardians get fee notifications. |
| BR-FI-007 | Fee non-payment consequences (e.g. withholding report cards) are a school-policy toggle, OFF by default, Head-controlled. **[ASSUMPTION A-10]** |

## Timetable (BR-TT)

| ID | Rule |
|---|---|
| BR-TT-001 | Within one term's timetable: a Teacher cannot occupy two slots at the same (day, period); neither can a Class. Validation is hard, override-free. |
| BR-TT-002 | Timetables are per (Class, Term); JHS 3 variants follow their calendar variant. |

## Library (BR-LI)

| ID | Rule |
|---|---|
| BR-LI-001 | Loan limits by borrower type (default: students 2, staff 5 **[ASSUMPTION A-11]**); an overdue loan blocks new loans. |
| BR-LI-002 | Loans track the physical BookCopy, not the title; lost/damaged copies change copy status with note. |

## Health (BR-HE)

| ID | Rule |
|---|---|
| BR-HE-001 | Full health records are visible only to NURSE, and to the student's Guardians. Teachers see only critical-alert flags. HEAD sees summaries (visit counts), not clinical detail. |
| BR-HE-002 | Medical visits marked "notify guardian" trigger notification to primary-contact guardians. |
| BR-HE-003 | Health data is never included in general exports/reports; it is excluded from analytics except anonymized counts. |

## Staff (BR-ST)

| ID | Rule |
|---|---|
| BR-ST-001 | Staff Number is immutable and unique. Teaching staff must have qualification records before subject assignment. |
| BR-ST-002 | Ending employment deactivates the account, unassigns future duties, but preserves all historical records (who taught what, who approved what). |

## Inventory (BR-IN)

| ID | Rule |
|---|---|
| BR-IN-001 | Stock quantities change only through recorded transactions (receipt, issue, write-off, return); computed stock is never negative. |

## Communication (BR-CO)

| ID | Rule |
|---|---|
| BR-CO-001 | SMS is the primary guardian channel; every Guardian record requires ≥ 1 phone number. Email is optional/secondary. |
| BR-CO-002 | Notifications are event-driven via a persistent outbox (ADR-008): no domain transaction directly calls an SMS gateway. |
| BR-CO-003 | Result-publication, invoice, and payment-receipt notifications are automatic; ad-hoc announcements require an authoring role (see permission matrix). |
| BR-CO-004 | All outbound messages are logged (template, recipient, status, provider reference) — cost and delivery are auditable. |

## Security & Audit (BR-SE)

| ID | Rule |
|---|---|
| BR-SE-001 | Children's data: least-privilege access per the permission matrix; guardian access strictly limited to own wards; no public endpoints expose student PII. |
| BR-SE-002 | Every create/update/delete on domain data and every approval/publication is written to the append-only Audit Log with actor, timestamp, entity, and change summary. |
| BR-SE-003 | No self-registration. Accounts are provisioned by workflows (admission, HR) or SYSTEM_ADMIN. |
| BR-SE-004 | Domain records are soft-deleted (status/archived flags). Hard deletion is reserved for DPA-compliant erasure requests, executed by SYSTEM_ADMIN with Head authorization, and itself audited. |
| BR-SE-005 | Authentication tokens are short-lived JWTs with refresh rotation; deactivation revokes refresh capability immediately (ADR-004). |

## Assumptions Index

**Status: A-01…A-11 confirmed by the listed owners on 2026-07-29.** The confirmed defaults below are now
binding rules; where marked "configurable" they remain per-year settings with these values as the starting
point. A-12 was discovered during WP-6 implementation (2026-07-30) and is proceeding on the documented
default pending confirmation, per CLAUDE.md's "record and proceed" rule.

| ID | Assumption | Owner | Status |
|---|---|---|---|
| A-01 | One class-teacher assignment per teacher per year | Head of School | Confirmed 2026-07-29 |
| A-02 | GES age-at-entry guidance applies with Head override | Head of School | Confirmed 2026-07-29 |
| A-03 | 30/70 SBA/Exam weighting default | Head of School | Confirmed 2026-07-29 |
| A-04 | Internal grade bands (A–F table above) | Head of School | Confirmed 2026-07-29 |
| A-05 | Student number format `UBS-<year>-<seq>` | School Admin | Confirmed 2026-07-29 |
| A-06 | Offer→enrollment requires initial payment | Head + Accountant | Confirmed 2026-07-29 |
| A-07 | LATE≈present, EXCUSED≈absent in aggregates | Head of School | Confirmed 2026-07-29 |
| A-08 | Auto-promotion default with exceptional repetition | Head of School | Confirmed 2026-07-29 |
| A-09 | Oldest-first payment allocation | Accountant | Confirmed 2026-07-29 |
| A-10 | Report-card withholding toggle default OFF | Head of School | Confirmed 2026-07-29 |
| A-11 | Library loan limits (2 student / 5 staff) | Librarian | Confirmed 2026-07-29 |
| A-12 | An exempted/N-A component is excluded from a student's category total (numerator and weight denominator), rescaled to 0–100 over the remaining scored components — rather than, e.g., contributing zero or blocking computation entirely | Head of School | Proposed 2026-07-30, pending confirmation |
