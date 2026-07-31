# ADR-006 — Academic Year/Term as First-Class Entities; Enrollment as the Academic Anchor

**Status:** Accepted · 2026-07-28

## Problem
How to model "which class is this student in?" so that history, promotion, and reporting stay correct across years.

## Context
Naive designs put `class_id` on the student and mutate it at promotion — destroying history ("what class was she in for 2026/27?" becomes unanswerable) and breaking term-scoped attendance/results/billing. Ghana's three-term year and JHS 3 calendar variant (BR-AS-003) demand explicit calendar entities.

## Options
1. Mutable `student.class_id`. 2. **Enrollment rows per (student, class, year)** with status lifecycle. 3. Full bitemporal modeling.

## Decision
Option 2 (doc 02 §5): Enrollment is the anchor; attendance, results, and invoices reference enrollments/terms, never "current class". All year-scoped associations (class teacher, subject teacher, HoD) follow the same rows-with-year pattern (doc 09 §5). Option 3 rejected as YAGNI — audit log covers "what did we believe when".

## Trade-offs
(+) Permanent academic history; promotion is *insert next year's enrollments*, not destructive update; transcripts derivable.
(−) "Current class" is a join away — mitigated with a standard roster query owned by `enrollment`.

## Future Implications
Alumni features and longitudinal analytics (FR-DASH-03) fall out of this model for free.
