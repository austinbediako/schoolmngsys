# 02 — Domain Model

> Terminology is defined once in the [Glossary](glossary.md). Rules referenced as `BR-…` live in
> [04 — Business Rules](04-business-rules.md). Persistence-level detail lives in
> [09 — Data Architecture](09-data-architecture.md).

## 1. Bounded Contexts

The domain divides into contexts with deliberately thin, well-named seams. Each maps to one module in [08 — Module Architecture](08-module-architecture.md).

| Context | Owns | Key invariants |
|---|---|---|
| **Identity & Access** | UserAccount, Role, Permission, sessions/tokens | A person may have 0..1 account; account ≠ person (ADR-004) |
| **Academic Structure** | AcademicYear, Term, TermCalendarVariant, Department, ClassLevel, Class, Subject, ClassSubjectAssignment | Exactly 3 terms/year; JHS 3 calendar variant (BR-AS-003) |
| **People** | Student, Guardian, StudentGuardian link, Staff | Student ≥1 active guardian link (BR-EN-004); immutable person numbers |
| **Enrollment & Admissions** | Application, AdmissionDecision, Enrollment | One active enrollment per student per year (BR-EN-001) |
| **Attendance** | AttendanceRecord, school-day calendar | Only on school days; one record/student/day (BR-AT-001/002) |
| **Assessment & Results** | AssessmentComponent, Score, TermResult, ReportCard, GradeScale, Mock exam series | Weighted 30/70 default (BR-AA-001); approval before publication (BR-AA-003) |
| **Promotion & Progression** | PromotionDecision, graduation records | End of Term 3 only (BR-PR-001) |
| **BECE** | BeceCandidate, registration exports, results import | JHS 3 only (BR-BE-001) |
| **Finance** | FeeSchedule, FeeItem, Invoice, InvoiceLine, Payment, PaymentAllocation, Adjustment | Immutable receipts; reversal-only corrections (BR-FI-003) |
| **Timetable** | Period template, TimetableSlot | No double-booking (BR-TT-001) |
| **Library** | Book, BookCopy, Loan | Loan limits; overdue blocks (BR-LI-001) |
| **Health** | HealthProfile, MedicalVisit | Restricted visibility (BR-HE-001) |
| **Staff Ops** | Staff records, qualifications, leave | — |
| **Inventory** | InventoryItem, StockTransaction | Stock never negative |
| **Communication** | MessageTemplate, NotificationOutbox, Announcement | Outbox pattern (ADR-008) |
| **Audit** | AuditLog | Append-only (BR-SE-002) |

## 2. Core Entity Catalog

Attributes listed are *conceptual*, not column lists.

### Academic Structure
- **AcademicYear** — label (`2026/2027`), start/end, status (PLANNED → ACTIVE → CLOSED). Exactly one ACTIVE at a time.
- **Term** — number (1–3), officialStart/End, expected school days. Belongs to one year.
- **TermCalendarVariant** — overrides term dates for a set of class levels (JHS 3 shortening).
- **Department** — NURSERY, KG, PRIMARY, JHS; HoD assignment per year.
- **ClassLevel** — ordered ladder (N1…JHS3) with canonical + Basic-numbering display names. Static reference data.
- **Class** — level + stream (e.g. Primary 3A) + capacity. Class-teacher assignment is per academic year.
- **Subject** — NaCCA subject with code; applicability by level range (e.g. OWOP is lower primary only).
- **ClassSubjectAssignment** — (Class, Subject, AcademicYear, Teacher): what is taught, to whom, by whom, this year.

### People
- **Student** — studentNumber (immutable, BR-EN-002), names, dateOfBirth, gender, photo, admission date, status (APPLICANT → ACTIVE → {TRANSFERRED_OUT, WITHDRAWN, GRADUATED, DECEASED}), documents (birth certificate etc.).
- **Guardian** — names, relationship comes from the link not the person, phone (required, BR-CO-001), email (optional), occupation, address.
- **StudentGuardian** — relationship type (MOTHER, FATHER, GRANDPARENT, AUNT/UNCLE, SIBLING, OTHER), isPrimaryContact, hasCustody, receivesBilling, receivesAcademicReports.
- **Staff** — staffNumber, names, staffType (TEACHING / NON_TEACHING), qualifications, GES registration number (optional), employment dates, status.

### Enrollment & Assessment
- **Enrollment** — (Student, Class, AcademicYear), status (ACTIVE, TRANSFERRED, WITHDRAWN, COMPLETED), rollNumber. The historical spine of the system: "which class was this child in, in 2027/28?" is always answerable.
- **AssessmentComponent** — (ClassSubjectAssignment, Term), title, category (SBA | EXAM), maxScore, weight within category, date.
- **Score** — (AssessmentComponent, Student), rawScore, enteredBy, enteredAt. Validated 0..maxScore (BR-AA-002).
- **TermResult** — computed snapshot: (Student, Subject, Term): sbaTotal, examTotal, weightedTotal, grade, subjectPosition, remark, status (DRAFT → SUBMITTED → HOD_APPROVED → PUBLISHED).
- **ReportCard** — per (Student, Term): aggregates TermResults, attendance summary, conduct/interest remarks, classPosition, headRemark, publishedAt.
- **GradeScale + GradeBand** — configurable per year (BR-AA-005); default scale documented as ASSUMPTION A-03.

### Finance
- **FeeSchedule** — (ClassLevel, Term, AcademicYear) → list of **FeeItem**s (name, amount, mandatory?).
- **Invoice** — per (Student, Term): lines from schedule ± **Adjustment** (discount, scholarship, waiver — each with reason + approver). Status: ISSUED → PART_PAID → PAID / has arrears carried forward.
- **Payment** — amount, channel (CASH, BANK, CHEQUE, MOMO), reference, receivedBy, receiptNumber (immutable). **PaymentAllocation** maps payments to invoices oldest-first (BR-FI-002).

### Operations
- **AttendanceRecord** — (Enrollment, date), status (PRESENT, ABSENT, LATE, EXCUSED), markedBy, markedAt.
- **TimetableSlot** — (Class, day-of-week, period) → (Subject, Teacher), per year/term.
- **BeceCandidate** — (Student, exam year), indexNumber, registration payload snapshot, results per subject (stanine 1–9) after import.
- **Book / BookCopy / Loan** — catalog vs physical copy; loan has due date, return date, condition notes.
- **HealthProfile / MedicalVisit** — see glossary; visits log nurse actions and guardian notification.
- **InventoryItem / StockTransaction** — quantity-tracked school property.
- **NotificationOutbox** — pending/sent messages with channel, recipient, template, correlation to domain event.
- **AuditLog** — actor, action, entityType/id, before/after summary, timestamp, IP.

## 3. Key Relationships (conceptual ERD)

```mermaid
erDiagram
    ACADEMIC_YEAR ||--|{ TERM : "has 3"
    ACADEMIC_YEAR ||--o{ ENROLLMENT : scopes
    CLASS_LEVEL ||--o{ CLASS : "instantiated as"
    DEPARTMENT ||--o{ CLASS_LEVEL : groups
    CLASS ||--o{ ENROLLMENT : receives
    STUDENT ||--o{ ENROLLMENT : "one active/year"
    STUDENT }o--o{ GUARDIAN : "via STUDENT_GUARDIAN"
    CLASS ||--o{ CLASS_SUBJECT : offers
    SUBJECT ||--o{ CLASS_SUBJECT : "taught as"
    STAFF ||--o{ CLASS_SUBJECT : teaches
    CLASS_SUBJECT ||--o{ ASSESSMENT_COMPONENT : "assessed by"
    ASSESSMENT_COMPONENT ||--o{ SCORE : records
    ENROLLMENT ||--o{ ATTENDANCE_RECORD : "daily"
    STUDENT ||--o{ TERM_RESULT : earns
    TERM_RESULT }|--|| REPORT_CARD : "aggregated into"
    STUDENT ||--o{ INVOICE : billed
    INVOICE ||--o{ PAYMENT_ALLOCATION : "settled by"
    PAYMENT ||--o{ PAYMENT_ALLOCATION : "split across"
    STUDENT ||--o| HEALTH_PROFILE : has
    STUDENT ||--o| BECE_CANDIDATE : "JHS3 only"
```

## 4. Domain Events

Events are the seams between modules — Communication and Audit subscribe rather than being called directly (ADR-008). Naming: past tense, `<Entity><Happened>`.

| Event | Emitted when | Primary consumers |
|---|---|---|
| `StudentAdmitted` | Admission decision accepted → enrollment created | Finance (create invoice), Communication (welcome SMS) |
| `EnrollmentCreated` / `EnrollmentEnded` | Year enrollment opens/closes | Analytics |
| `AttendanceMarked` | Register submitted for a class/day | Analytics; Communication (absence alert, post-MVP) |
| `ScoresSubmitted` | Teacher submits a subject's scores for term | Results workflow (moves to SUBMITTED) |
| `TermResultsPublished` | Head approves & publishes a class's results | Communication (guardian SMS), Parent portal visibility |
| `StudentPromoted` / `StudentRepeated` | Promotion decisions finalized | Enrollment (next-year classes), Communication |
| `StudentGraduated` | JHS 3 exit processing | Records/transcripts |
| `InvoiceIssued` | Term billing run | Communication (fee notice) |
| `PaymentReceived` | Payment recorded | Communication (receipt SMS), Finance analytics |
| `BeceCandidateRegistered` / `BeceResultsImported` | BECE cycle steps | Records, Analytics |
| `MedicalVisitRecorded` (guardian-notify flag) | Nurse logs a visit | Communication |
| `UserAccountCreated` / `RoleAssigned` | IAM changes | Audit (always), Communication (credentials delivery) |

Every event is also written to the Audit context (BR-SE-002).

## 5. Aggregate & Consistency Notes

- **Enrollment is the academic anchor.** Attendance, results, and invoices hang off (Student × Year/Term) through Enrollment — never off "the student's current class", which is a derived notion.
- **TermResult is a snapshot, not a live view.** Once PUBLISHED it never changes silently; corrections create a new revision with audit trail (BR-AA-006).
- **Financial records are event-sourced in spirit**: invoices and payments append; corrections are compensating entries (BR-FI-003).
- **Reference data (ClassLevel, Subject list) is code-managed** via migrations, not user-editable CRUD, to keep the GES/NaCCA ladder trustworthy. Subjects may be *activated/deactivated* per year, not redefined.
