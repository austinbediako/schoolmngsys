# Glossary — Canonical Domain Language

> **This is the single source of truth for terminology.** Every other document uses these terms
> exactly as defined here. If a term needs a new meaning, change it here first, then update usages.
> Cross-references: [Domain Model](02-domain-model.md), [Business Rules](04-business-rules.md).

## Institutional & Regulatory

| Term | Definition |
|---|---|
| **UBS** | University Basic School, Legon — the single school this system serves. Operates Nursery through JHS 3 on the University of Ghana campus. |
| **UBS-LMIS** | University Basic School Legon Integrated Management Information System — this project. |
| **GES** | Ghana Education Service. Governs the national school calendar, promotion norms, and general basic-education policy. |
| **NaCCA** | National Council for Curriculum and Assessment. Sets the standards-based curriculum and subject list. |
| **WAEC** | West African Examinations Council. Administers the BECE. |
| **FCUBE** | Free Compulsory Universal Basic Education — Ghana's 11-year compulsory cycle: KG 1 → JHS 3. |
| **BECE** | Basic Education Certificate Examination. Taken by JHS 3 students; WAEC-administered; graded on a 1–9 stanine scale per subject (1 is best). |
| **Common Core Programme (CCP)** | NaCCA curriculum phase covering Basic 7–9 (JHS 1–3). |
| **Ghana Data Protection Act (Act 843, 2012)** | The privacy law governing personal data in Ghana; the system processes children's data and must comply. |

## Academic Structure

| Term | Definition |
|---|---|
| **Academic Year** | A school year, e.g. `2026/2027`, running roughly September–August, containing exactly three Terms. First-class entity; all academic records are scoped to one. |
| **Term** | One of three divisions of an Academic Year (Term 1, Term 2, Term 3), each with an official start date, end date, and count of school days. |
| **Term Calendar Variant** | A per-cohort override of term dates. Exists because JHS 3's Terms 2–3 end earlier than the rest of the school (BECE preparation). |
| **Department** | One of the four organizational units: **Nursery**, **KG**, **Primary**, **JHS**. Each has a Head of Department. |
| **Class Level** | A rung on the academic ladder: Nursery 1, Nursery 2, KG 1, KG 2, Primary 1–6, JHS 1–3. Ordered; promotion moves a student to the next level. |
| **Basic 1–9** | Alternative GES naming: Basic 1–6 = Primary 1–6; Basic 7–9 = JHS 1–3. The system stores one canonical level and displays either name. |
| **Class** | A concrete teaching group at a Class Level, possibly one of several parallel **streams** (e.g. "Primary 3A", "Primary 3B"). |
| **Stream** | The letter suffix distinguishing parallel classes at the same level (A, B, …). |
| **Class Teacher** | The staff member with pastoral and administrative responsibility for one Class in one Academic Year (register, report card remarks, parents' first contact). |
| **Subject Teacher** | A teacher assigned to teach one Subject to one Class. In Nursery/KG/lower primary, the Class Teacher usually teaches most subjects; at JHS, subjects have specialist teachers. |
| **HoD (Head of Department)** | A senior teacher who oversees a Department; approves results within the department and supervises its teachers. |
| **Head of School** | The Headmaster/Headmistress. Final approver of results, promotions, and admissions. Also called "School Head". |
| **Enrollment** | The association of a Student with a Class for one Academic Year. A student has exactly one active enrollment per year. Enrollments are never overwritten — history is preserved. |
| **Subject** | A curriculum subject per NaCCA (e.g. English Language, Mathematics, Integrated Science, OWOP, RME, Ghanaian Language, French, Computing, Career Technology, Creative Arts & Design, Physical Education). Offered per Class per Year. |
| **OWOP** | Our World Our People — a lower-primary NaCCA subject. |
| **RME** | Religious and Moral Education. |

## Assessment & Results

| Term | Definition |
|---|---|
| **SBA (School-Based Assessment)** | Continuous assessment conducted during the term (class tests, projects, homework, group work). Also historically called "Continuous Assessment (CA)". Contributes a configurable weight (default 30%) to the Term Result. |
| **End-of-Term Exam** | The examination written at the close of each term. Contributes the remaining weight (default 70%). |
| **Assessment Component** | A single scored piece of work (one class test, one project, the end-of-term exam) with a maximum raw score and a category (SBA or Exam). |
| **Term Result** | The computed outcome for one Student, one Subject, one Term: weighted total (0–100), grade, subject position, and teacher remark. |
| **Report Card (Terminal Report)** | The per-student, per-term document aggregating all Term Results plus attendance summary, conduct, interest, class teacher and head remarks, and Class Position. |
| **Class Position** | The student's rank within their Class by average of subject totals for the term (ties share a rank). |
| **Grade Scale** | A configurable mapping from score bands to grades/descriptors. Internal reports use the school's scale; BECE uses WAEC's stanine 1–9. |
| **Mock Exam** | School-organized BECE rehearsal for JHS 3, typically in Terms 1–2 of the final year. Managed as a distinguishable exam series, not a normal term exam. |
| **Promotion** | Moving a student to the next Class Level for the following Academic Year, decided at the end of Term 3. |
| **Repetition** | Retaining a student at the same Class Level for another year; exceptional, requires Head approval and guardian consultation. |
| **Graduation** | Completion of JHS 3 (exit of the basic cycle), evidenced by BECE participation and a leaving certificate/transcript. |

## People

| Term | Definition |
|---|---|
| **Student** | A learner enrolled at UBS. Identified by an immutable **Student Number**. |
| **Guardian** | A parent or other adult legally responsible for one or more Students. One Student may have multiple Guardians; one Guardian may have multiple **Wards**. |
| **Ward** | A Student, from the perspective of their Guardian. |
| **Staff** | Any employee: teaching staff (teachers, HoDs, Head) and non-teaching staff (accountant, librarian, nurse, administrative staff). Identified by a **Staff Number**. |
| **User Account** | Login credentials + roles. Distinct from the person (Student/Guardian/Staff) it belongs to; a person may exist without an account (e.g. Nursery students never log in). |

## Finance

| Term | Definition |
|---|---|
| **Fee Schedule** | The set of Fee Items applicable to one Class Level for one Term of one Academic Year. |
| **Fee Item** | A single charge line (e.g. Tuition, PTA Dues, ICT Levy, Examination Fee, Feeding). |
| **Invoice (Bill)** | The amount owed by a Student for a Term, generated from the Fee Schedule plus any student-specific adjustments (discounts, scholarships). |
| **Payment** | Money received against invoices, via cash, bank, cheque, or **Mobile Money (MoMo)**. Every payment produces an immutable receipt. |
| **Arrears** | Unpaid invoice balance carried forward. |
| **Part Payment** | A payment smaller than the outstanding balance; explicitly allowed. |

## Operations

| Term | Definition |
|---|---|
| **Attendance Register** | The daily per-class record of each student's status: Present, Absent, Late, or Excused. Taken once per school day by the Class Teacher. |
| **School Day** | A calendar day on which classes hold, derived from the Term calendar minus holidays/closures. Attendance can only exist on school days. |
| **Timetable** | The weekly grid of periods assigning (Class, Subject, Teacher) to (Day, Period slot). |
| **Admission** | The workflow taking an applicant from application through assessment to enrollment as a Student. |
| **Health Profile** | A student's standing medical record: blood group, allergies, chronic conditions, immunizations, emergency contacts. |
| **Medical Visit** | A dated encounter with the School Nurse (complaint, action, referral, guardian-notified flag). |
| **Audit Log** | The append-only record of who changed what, when, from where. Never edited or deleted. |
| **Notification** | An outbound message to a user (SMS, email, in-app). SMS is the primary channel for guardians in Ghana. |
