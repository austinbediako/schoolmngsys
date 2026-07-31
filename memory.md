# memory.md — Persistent Project Memory

> Long-term memory of UBS-LMIS. Append/update as knowledge solidifies; never let it contradict
> [docs/](docs/README.md) — when in doubt, the canonical doc wins and this file points to it.
> Session bootstrap and knowledge-ownership rules: [CONTEXT.md](CONTEXT.md). Last updated: 2026-07-30.

## WP-0 (shared foundations) — implementation notes

Phase 1 coding started 2026-07-29; WP-0 is complete and green against real PostgreSQL (Testcontainers).
Facts a future session needs, not derivable from a diff:

- **Spring Boot 4.1 pulls Hibernate 7.4.1 and Testcontainers 2.0.5** (not the 1.x line) — both managed
  transitively via `spring-boot-dependencies`; do not add a competing `testcontainers-bom` import, it
  will pin an older docker-java that fails to negotiate the Docker API version against current Docker
  Desktop engines. Testcontainers 2.x also renamed its module artifacts: `org.testcontainers:testcontainers-junit-jupiter`
  and `org.testcontainers:testcontainers-postgresql` (not the old unprefixed `junit-jupiter`/`postgresql`).
- **UUIDv7 PKs use Hibernate's native `@UuidGenerator(style = UuidGenerator.Style.VERSION_7)`** — no
  custom generator needed; Hibernate 7 ships this directly.
- **Boot 4 renamed/split several starters** from their Boot 3-era names: AOP is now
  `spring-boot-starter-aspectj` (not `-aop`); `@WebMvcTest`/`@AutoConfigureMockMvc` moved to
  `org.springframework.boot.webmvc.test.autoconfigure` (not `...test.autoconfigure.web.servlet`).
- **Audit interception design** (`shared.audit.Audited` marker + `audit` module's `AuditAspect` +
  `AuditRecorder`): the recorder uses `@Transactional(propagation = MANDATORY)` deliberately, so an
  `@Audited` method that isn't already running inside a transaction fails loudly instead of writing
  audit in a silently separate transaction from the mutation it describes (BR-SE-002/ADR-007).
- **`CurrentActorProvider` contract for WP-1**: `SecurityContextCurrentActorProvider` (shared/security)
  expects `Authentication.getName()` to return the account id as a UUID string (e.g. JWT `sub` claim).
  WP-1's auth wiring must honor this or `createdBy`/`updatedBy`/audit actor attribution silently no-ops.
- **Test-only fixture pattern established**: entities/tables needed purely to exercise shared
  infrastructure (e.g. `TestWidget`/`test_widgets`) live under `src/test/java` + a
  `src/test/resources/db/testmigration/V9xx__*.sql` migration, wired in via `@TestPropertySource`
  on `AbstractIntegrationTest` (`spring.flyway.locations=classpath:db/migration,classpath:db/testmigration`)
  — **never** a `src/test/resources/application.yml`: a same-named file on the test classpath
  *replaces*, not layers on top of, `src/main/resources/application.yml`, silently dropping every
  other property (this broke all of WP-1's `app.jwt.*`/`app.auth.*` config until traced down).
  Never invent a real domain entity early just to test infrastructure.
- ArchUnit rules for `no_entities_in_api` / `service_does_not_depend_on_api` use `allowEmptyShould(true)`
  since no `api` package exists until WP-1+ — they start actually enforcing once controllers appear.
- **Testcontainers "singleton container" gotcha**: a plain `static @Container` field inherited from
  a shared base class looks JVM-wide but isn't — `TestcontainersExtension` calls `.stop()` on it in
  `afterAll` of *every* test class that uses it, and restarting the same container instance for the
  next class is unreliable (intermittent connection-refused after ~30-60s pool-acquisition timeout,
  only under a full multi-class suite run — passes standalone every time, which is the tell). Fixed
  via `PostgresTestContainer` (`src/test/java/.../PostgresTestContainer.java`): a true singleton that
  overrides `stop()` to no-op, so every class's stop-attempt is harmless and Ryuk reaps the real
  container at JVM exit. `AbstractIntegrationTest` uses `PostgresTestContainer.instance()` with
  `@ServiceConnection` only — no `@Testcontainers`/`@Container` annotations needed once you manage
  lifecycle yourself.

## WP-1 (auth) — implementation notes

WP-1 is complete and green (19/19 tests) as of 2026-07-29. Facts a future session needs:

- **Account ≠ person, enforced structurally**: `Account` stores `(personType, personId)` as an
  opaque reference (no FK) since `people` (WP-3) doesn't exist yet. `PersonType` lives in
  `shared.security`, not `auth.domain` — every future module's scope filter needs this
  discriminator, and no module may depend on `auth` directly (docs/08 §3: auth is invoked via
  security infrastructure, never called directly).
- **Entities never leak into `api`** (ArchUnit-enforced, docs/08 §2): service methods return
  entity-free view records (`AccountView`, `RoleView`) via `AccountQueryService`/`RoleQueryService`;
  `api`-package DTOs map from those views, never from `Account`/`Role` entities directly.
- **Audit write survives the transaction that denies the request**: a denied login (bad password,
  lockout, deactivated) always ends by throwing an unchecked `AuthenticationException` from inside
  `@Transactional login()` — which rolls back everything that same method wrote, including the
  failed-attempt counter and lockout state. Fixed with `AuthAttemptRecorder`
  (`@Transactional(propagation = REQUIRES_NEW)`), a separate bean so the write commits before the
  exception propagates. Same pattern (self-invocation bypasses the transactional proxy) applies
  anywhere a service intends to persist state on a path that then throws.
- **JWT via JJWT 0.12.6**: `Jwts.builder().subject(...).claim(...).signWith(SecretKey).compact()` /
  `Jwts.parser().verifyWith(SecretKey).build().parseSignedClaims(token)`; `Keys.hmacShaKeyFor(bytes)`
  builds the key. Access token embeds `personType`/`personId`/`permissions` claims; refresh tokens
  are opaque random values, only their SHA-256 hash is persisted (`TokenHasher`).
- **`CurrentAccountProvider`/`AuthenticatedAccount`** (shared/security) is the scope-filter
  mechanism WP-2+ builds on: `JwtAuthenticationFilter` sets `Authentication` principal = account id
  string (honoring WP-0's `CurrentActorProvider` contract) and `.setDetails(AccountAuthenticationDetails)`
  carrying `personType`/`personId`; `SecurityContextCurrentAccountProvider` reads both back out.
  The *what to filter by* logic (own classes, own wards, department) is each future module's job —
  this only answers *who is asking*.
- **Spring Security 7.1 (Boot 4.1) package moves**: `UsernamePasswordAuthenticationFilter` is under
  `org.springframework.security.web.authentication`, not `...security.authentication` (that package
  now only has token/exception types like `UsernamePasswordAuthenticationToken`,
  `BadCredentialsException`, `LockedException`, `DisabledException` — all of which double as the
  auth-required problem-type trigger via WP-0's existing `AuthenticationException` handler, so login
  failures don't need bespoke exception types).
- **Jackson 3, not `com.fasterxml`**: Spring Framework 7 moved to Jackson 3 under `tools.jackson.*`
  (`tools.jackson.databind.ObjectMapper`, `JacksonException` is unchecked). The old
  `com.fasterxml.jackson.databind` only appears transitively via `jjwt-jackson` (runtime scope, not
  compile-visible) — don't reach for it in application code.
- **Password hashing: BCrypt**, not Argon2id, chosen for lower resource use on modest hosting
  (memory-hard Argon2 is heavier); NFR-11 permits either.
- **OTP delivery is a logging stub** (`LoggingOtpSender`) pending the WP-8 SMS outbox — the OTP is
  logged at WARN, clearly marked dev-only. Replace when communication/ADR-008 lands.
- Default access-token TTL was corrected from a stale 24h to **15 min** (docs/11 §2) — the original
  `JWT_EXPIRATION_MS` default in `.env.example`/`application.yml` predated the security doc and had
  never been reconciled.
- Added the `DataIntegrityViolationException` -> `conflict` problem handler to `GlobalExceptionHandler`
  — docs/10 §3 always required this ("constraint violations map to conflict, never leak SQL") but
  WP-0 never actually implemented it; WP-2's unique constraints were the first thing to exercise it.

## WP-2 (academics) — implementation notes

WP-2 is complete and green (28/28 tests) as of 2026-07-29. Facts a future session needs:

- **`SchoolClass`, not `Class`**: the domain entity for docs' "Class" concept is named `SchoolClass`
  to avoid shadowing `java.lang.Class` in every file that references it (table is still `classes`).
- **Class is durable across years; only the teacher assignment and enrollment are year-scoped**
  (docs/02 §5, ADR-006): `SchoolClass` has no year FK. `ClassTeacherAssignment` and
  `ClassSubjectOffering` carry `academicYear` and are what gets created fresh each year.
  `teacherStaffId` on both is an opaque UUID (no FK) — `people`/Staff (WP-3) doesn't exist yet,
  same pattern as `auth.Account.personId` from WP-1.
- **BR-AS-005/A-01 (one class teacher per class per year, one class per teacher per year)** is
  enforced in both directions: a friendly `BusinessRuleViolationException` check in
  `ClassService.assignClassTeacher` first, then a partial unique index each direction
  (`uq_class_teacher_assignments_class_id_academic_year_id` and
  `..._teacher_staff_id_academic_year_id`) as the last net (docs/09 §4 pattern).
  BR-AS-001 (exactly one ACTIVE year) follows the same shape:
  `uq_academic_years_one_active` is a single-column partial unique index on `status` where
  `status = 'ACTIVE'` — valid because every matching row has the same value, so at most one can exist.
- **Term Calendar Variant resolution** (BR-AS-003, JHS 3's earlier Terms 2-3): `TermCalendarService
  .resolveTermDateRange(term, classLevel)` looks up a `TermCalendarVariant` for that
  `(term, classLevel)` pair and falls back to the term's official dates — this is the shape every
  future per-level-date override would follow, not just JHS 3.
- **School-day computation** (FR-ACAD-02) is intentionally layered: weekend check (Sat/Sun, no
  business-rule backing needed — implicit in "school day" universally) → `school_day_exceptions`
  lookup (explicit holiday/closure) → term-range membership (resolved per class level via the
  variant logic above). All three must pass for a date to count as a school day.
- **Reference data seeded, not user-editable** (docs/02 §5): `class_levels` (fixed N1..B9 ladder
  with `sequence` for ordering — WP-9 promotion will walk this), `departments`, and `subjects`
  (NaCCA list, applicability as a `[minLevelSequence, maxLevelSequence]` range) all come from `V4`.
  The subject-applicability ranges in `V4__academic_reference_data.sql` are starter defaults
  (flagged in a migration comment), not authoritative curriculum policy — refine via a later
  migration if the school corrects them, never edit the seed row in place.
- **BR-AS-007 (year closure) is only partially enforceable yet**: `AcademicYearService.closeYear`
  checks status == ACTIVE and marks CLOSED, audited — the full precondition checklist (all term
  results published/voided, promotions finalized) becomes enforceable once WP-6 (assessment) and
  WP-9 (progression) exist. This is a deliberate incremental gap, flagged in a code comment, not an
  oversight.
- **Entity-free views again** (same ArchUnit-enforced pattern as WP-1): `AcademicYearView`,
  `TermView`, `ClassView`, `SubjectView`, `ClassTeacherAssignmentView`, `ClassSubjectOfferingView`
  in `academics.service`; `api`-package DTOs wrap those, never the entities directly.

## WP-3 (people) — implementation notes

WP-3 is complete and green (38/38 tests) as of 2026-07-30. Facts a future session needs:

- **BR-EN-004 (every ACTIVE student needs >=1 guardian, >=1 primary contact) is enforced
  atomically, not as a DB constraint**: `StudentService.createStudent` requires the caller to
  supply the initial guardian link(s) in the *same* call — a student is never even briefly
  guardian-less — and `StudentGuardianLinkService.unlink` re-checks the invariant on removal
  (refuses to drop the last link, or the last primary-contact link). A cross-table "at least one
  row where X" invariant like this can't be a simple CHECK constraint, so the service layer is the
  enforcement point, not the schema.
- **`GuardianWardResolutionService` is the officially named scope-filter source** (docs/08 §4:
  people "exposes... guardian-ward resolution (authorization scope source)"). Every future module
  scoping a GUARDIAN-role caller (attendance, results, finance) must call
  `resolveWardIds(guardianId)` / `isWardOf(...)` here — never query `student_guardians` directly,
  since that repository is private to `people`.
- **Student number generation** (BR-EN-002/A-05, `UBS-<entryYear>-<sequence>`): sequence is
  zero-padded to 4 digits (`UBS-2026-0001`) — the confirmed assumption specifies the format but not
  padding width; this is an implementation choice within it, not a new assumption. Immutability is
  structural: `Student.studentNumber` has no setter and the column is `updatable = false`.
  **Staff numbers have no such generator** — BR-ST-001 only requires immutable+unique, no format
  rule was ever confirmed, so `createStaff` takes a caller-supplied number (admin-assigned).
- **Document storage decision made here** (was flagged "before WP-3" in docs/14 §8): filesystem,
  behind a `DocumentStorage` interface (`people.service`) with `FilesystemDocumentStorage` as the
  only implementation — same interim-seam pattern as WP-1's `OtpSender`. Object storage later is a
  new implementation behind the same interface, not a rewrite. Base directory is
  `app.storage.documents-dir` (default `./data/documents`). Scoped to students only —
  `STUDENT_DOCUMENT_VIEW`/`UPLOAD` are the only document permissions in the catalog; no
  staff-document feature exists.
- **`Staff.endEmployment()` does not cascade to account deactivation.** BR-ST-002 talks about
  preserving history (satisfied — nothing is deleted, only status/end-date change), but
  auto-deactivating the linked `auth.Account` would require either a cross-module event `people`
  doesn't currently publish (docs/02 §4's event table has no "staff employment ended" event — adding
  one wasn't invented here, per "documentation before invention") or a direct call into `auth`,
  which no module boundary permits. For now, ending a staff member's employment and deactivating
  their account are two separate admin actions (`POST .../staff/{id}/end-employment` and
  `POST .../accounts/{id}/deactivate` from WP-1). Revisit if a future WP needs the cascade —
  add the event to docs/02 §4 first, then wire an `auth`-side listener.
- **Staff qualifications are recorded but not enforced against subject assignment.** BR-ST-001 says
  teaching staff need qualifications on file before subject assignment, but `academics` has no
  dependency on `people` in the module boundary diagram (docs/08 §3) — enforcing this cross-module
  would require adding that edge. Left as a process control for now, not code-enforced.

## WP-4 (enrollment) — implementation notes

WP-4 is complete and green (44/44 tests) as of 2026-07-30. **This closes M1 (walking skeleton)** —
WP-0 through WP-4 all done, well beyond the originally-scoped "thin slice"; academics/people were
each built out fully rather than minimally, so the M1 exit criterion (stack, auth, migrations,
error format, audit, tests all proven end to end) is satisfied with real breadth behind it.

- **`Enrollment` uses plain UUID fields (`studentId`, `classId`, `academicYearId`), not JPA
  `@ManyToOne` associations to `people`/`academics` entities.** This is the first module that
  legitimately *depends* on other modules (docs/08 §3: ENR --> PPL, ENR --> ACAD), unlike
  `academics`/`people` which have no outgoing dependency — but "depends on" means "calls their
  service", not "imports their entity class". Composing via `StudentQueryService.get(id)` /
  `ClassQueryService.get(id)` / `AcademicYearQueryService.get(id)` (existence checks, thrown as
  `NotFoundException`) keeps every module's entities private to itself, consistent with every
  other cross-module reference so far (`auth.Account.personId`, `academics.*.teacherStaffId`).
- **Two parallel status concepts, deliberately**: `Enrollment.status` (ACTIVE/TRANSFERRED/WITHDRAWN/COMPLETED)
  is per-year and lives in `enrollment`; `Student.status` (.../TRANSFERRED_OUT/WITHDRAWN/...) is the
  student's overall lifecycle state and lives in `people`. `EnrollmentService.recordExit(...)` owns
  the reason+date (BR-EN-005) and, after transitioning its own row, calls `people`'s
  `StudentService.markTransferredOut`/`markWithdrawn` to keep the mirror in sync — added two new
  methods to `people.Student`/`StudentService` this WP (`transferOut()`/`withdraw()`, no reason/date
  params, since those belong to the enrollment record) that WP-3 deliberately left out with a
  "status transitions belong to enrollment" comment. Going back to touch a "finished" module here
  was expected, not scope creep — this is exactly the ENR->PPL dependency the architecture predicted.
- **BR-EN-001 (one active enrollment per student per year)** follows the now-familiar shape:
  friendly `BusinessRuleViolationException` check first, `uq_enrollments_student_year_active`
  partial unique index (`WHERE status = 'ACTIVE'`) as the last net.
- **"Transfer-in/out" scoping**: interpreted as the two FR-STU-04 exit reasons (`TRANSFERRED` for
  transfer to another school, `WITHDRAWN` for leaving without transferring) plus ordinary
  mid-year `enroll()` calls for students joining after the school year has started — not a
  same-school "move between classes" feature, since nothing in docs/04/05 asks for that.
  Roster (`EnrollmentQueryService.roster`) filters to ACTIVE only; history
  (`EnrollmentQueryService.history`) returns everything, so exited students disappear from the
  roster while the full record stays queryable — same "history is sacred" pattern as everywhere else.
- **Deliberately not built**: FR-STU-04's "generate a transfer summary document (data export)" —
  no templating/PDF library has been chosen and it's not in the WP-4 test plan (docs/14 §5). The
  exit-recording workflow itself (reason, date, status) is complete; document generation is a
  separate, later concern.
- **No first-admin-account bootstrap exists yet.** `AccountController.create` requires
  `ACCOUNT_CREATE`, which requires already being authenticated as someone holding it — there's no
  seed/bootstrap account, so a fresh database has no way to create its first `SYSTEM_ADMIN` via the
  API alone. Not a WP-4 (or any WP so far) requirement, but worth flagging before M5 deployment —
  needs either a Flyway-seeded bootstrap account+known temp password, or a one-off CLI/admin
  command.

## WP-5 (attendance) — implementation notes

WP-5 is complete and green (50/50 tests) as of 2026-07-30.

- **`AttendanceRecord` is keyed by `enrollmentId`, not `(studentId, classId)` separately** — since
  a student has at most one ACTIVE enrollment per year (BR-EN-001), the enrollment IS the
  student-in-this-class-this-year identity, so `(enrollment_id, attendance_date)` alone gives
  BR-AT-001's "one record per student per school day". `enrollmentId` is a plain UUID (same
  cross-module pattern as `Enrollment` itself) — but unlike `Enrollment`/`Student`, this one has a
  real DB foreign key to `enrollments(id)`, since attendance genuinely never exists without a real
  enrollment (no "opaque forward reference to a module that doesn't exist yet" concern here).
  **Test takeaway**: any test inserting an `AttendanceRecord` directly via the repository (to set
  up multi-date fixtures `markRegister` can't produce in one call) must use a real enrollment id,
  not `UUID.randomUUID()` — the FK constraint will reject a fake one.
- **`Clock` is now an injectable bean** (`shared.config.ClockConfig`), and `AttendanceService` uses
  `LocalDate.now(clock)` instead of `LocalDate.now()` — the first time-relative business rule
  (BR-AT-004: "only today's register can be marked directly") needed this for deterministic tests.
  Tests override it with a `@TestConfiguration` `@Bean @Primary` on a *differently-named* method
  (e.g. `fixedClock()`, not `clock()` — reusing the same bean name throws
  `BeanDefinitionOverrideException` since bean-definition overriding is off by default; `@Primary`
  resolves ambiguity between two *same-type, different-name* beans, it does not permit two
  same-name definitions). Any other genuinely time-relative rule later should reuse this `Clock`
  rather than calling `LocalDate.now()`/`Instant.now()` directly.
- **"Duplicate-day rejection" means duplicate *register submission*, not duplicate individual
  records**: `markRegister` is an all-or-nothing bulk submit that "locks" the day (WF-03) — a
  second bulk submit for the same class+date is rejected outright (BR-AT-001), even though
  same-day *corrections* to individual records are explicitly allowed by the same workflow.
  Two separate methods encode the two different permission/workflow shapes: `correctSameDay`
  (today only, `ATTENDANCE_MARK`, reason optional) vs `correctPastRecord` (any date, gated by
  `ATTENDANCE_CORRECT` at the API layer, reason mandatory — BR-AT-004).
- **A-07 aggregation** (`AttendanceSummaryService`): LATE counts as present, EXCUSED counts as
  absent, matching the confirmed assumption exactly. `totalSchoolDays` in the summary is simply the
  count of records taken for that enrollment — it assumes the class was marked every school day; it
  does not cross-check against the term calendar's expected school-day count. Fine for MVP, worth
  revisiting if a school stops marking attendance for a stretch and the report-card math needs the
  true expected denominator instead.
- **Deliberately not built**: the post-MVP per-subject JHS attendance variant (G-07) and same-day
  guardian SMS on absence (explicitly marked post-MVP in WF-03) — both correctly out of scope.

## WP-6 (assessment) — implementation notes

WP-6 is complete and green (66/66 tests) as of 2026-07-30 — the "highest-value suite" per the WP-6
test plan, and it earned that label: this is the most intricate business logic in the codebase so far.

- **`GradeScale` bundles the A-03 weighting AND the A-04 bands into one per-year entity** — docs
  describe these as two separate concepts, but both are "configurable per Academic Year" and always
  configured together in practice, so splitting them would just add a join for no benefit.
- **The V10 migration seeds a year-independent `default_grade_bands` *template*, not real per-year
  data** — docs/14 §4 originally planned "seed the first academic year's grade scale," which is
  impossible at migration time (no academic year exists yet). `GradeScaleService.createDefault
  (academicYearId)` copies the template into a real `grade_scales`/`grade_bands` row set, as an
  explicit WF-01 step-5 admin action — this is the second time a docs/14 migration plan needed
  correcting against a real constraint (the first was WP-3's document storage decision); both are
  now reflected in docs/14 §4/§8 directly, not just here.
- **A-12 recorded, not invented**: how an exempted/N-A component affects a student's category total
  was genuinely unspecified by any BR/FR. Proceeding on a documented default (excluded from both
  numerator and weight denominator, remainder rescaled to 0-100) per CLAUDE.md's "record and
  proceed" rule — added to docs/04's assumptions index as **Proposed**, not Confirmed, since (unlike
  A-01…A-11) no stakeholder has actually seen this one yet. Flag it for review before relying on it
  for a real published result.
- **Real Hibernate flush-ordering bug found by the tests, not by inspection**: `ResultRevisionService
  .revise()` originally saved the new (revised) `TermResult` before archiving the old one. Hibernate
  flushes all pending INSERTs before any UPDATEs in a single flush *regardless of call order* — so
  the new row's INSERT ran before the old row's archive-UPDATE, and both rows were briefly "current"
  under the partial unique index (`uq_term_results_enrollment_offering_term_current`), violating it.
  Fixed by archiving+`saveAndFlush`-ing the old row *first*, forcing that UPDATE to land before the
  new INSERT is even queued — still one transaction, so a later failure still rolls back atomically.
  **General lesson**: whenever two rows must never simultaneously satisfy the same partial unique
  index, don't trust JPA save-call order — force the ordering with an explicit flush, or avoid the
  overlap window entirely.
- **BR-AA-004 (class position) reuses the exact same competition-ranking algorithm as BR-AA-004's
  subject position** (1, 2, 2, 4) — `ResultComputationService.assignSubjectPositions` and
  `ReportCardService.publishForClass`'s ranking loop are structurally identical; not extracted into
  a shared utility for two call sites, but worth doing if a third one appears.
- **Publishing is class-wide, not per-subject**: `ResultPipelineService.publishClassResults` requires
  *every* subject offering's results for that class+term to be `HOD_APPROVED` before publishing any
  of them — matching WF-04's "Head PUBLISHes class" (a single act, not one Head decision per
  subject). `approveSubjectResults` is the one HoD-scoped step, per subject.
- **Revision reuses `TermResultsPublished`** rather than a new event type — a revision is, from the
  guardian-notification/report-card perspective, indistinguishable from a (re-)publication of that
  class/term's results.
- **Deliberately not built**: BR-AA-008 (mock exams, JHS 3 only, post-MVP-adjacent but separate
  series) — no FR-RES item requires it in Phase 1, and no mock-exam entity exists yet; revisit if a
  future WP needs it. Report-card PDF rendering is explicitly a frontend concern (FR-RES-05).

## WP-7 (finance) — implementation notes

WP-7 is complete and green (10/10 finance tests, 76/76 full suite) as of 2026-07-30 — the second and
final M4 work package, closing Phase 1's finance track.

- **Receipt/payment immutability is application-only, not a DB trigger** (the docs/14 §8 decision
  made this WP): `Payment.amount`/`channel`/`reference`/`receiptNumber` are `updatable = false` at
  the JPA level, and the service layer has no update path at all — `PaymentService` only ever
  inserts (`Payment.original`, `Payment.reversalOf`). Corrections are exclusively a new reversal
  `Payment` row (negative amount, mirrored negative `PaymentAllocation` rows), matching the same
  pattern already used for `Student.studentNumber` and `TermResult` revisions.
- **Arrears carry-forward (BR-FI-005) is deliberately NOT a duplicated invoice line item.** Each
  term's billing run (`BillingRunService`) creates one fresh `Invoice` per active enrollment from
  that (class level, term)'s APPROVED `FeeSchedule`; an older unpaid invoice from a prior term is
  simply left open. `InvoiceRepository.findByEnrollmentIdAndStatusNotAndArchivedAtIsNullOrderByIssuedAtAsc`
  (excludes `PAID`, oldest first) is what `PaymentService.recordPayment` allocates against by
  default — that ordering *is* the carry-forward mechanism, so a payment naturally clears the oldest
  arrears before touching the current term's invoice. No risk of double-counting a balance that a
  duplicated line item would have created.
- **`InvoiceLedgerService` is the one place that turns (lines, allocations) into balance/status** —
  every mutator that touches either side (billing, payment allocation, reversal, adjustment
  approval) calls back through `refreshStatus` rather than recomputing the cached `Invoice.status`
  independently. Reversal allocations are stored as negative `PaymentAllocation` rows, so a plain
  sum nets them out automatically without any reversal-specific branching in the ledger math.
- **`FinanceQueryService.arrears`/`cashBook` (FR-FIN-06) were scoped down from "financial reports"
  in general to the two report types the WP-7 test plan bullet actually names** (arrears aging,
  daily cash book) plus per-invoice/per-enrollment balance views already needed by payments — a full
  collection-by-period/class/fee-item BI layer is not built and would be a distinct future WP if
  the school asks for it; not invented speculatively here.
- **Finance formally depends on academics** (`FIN --> ACAD` added to docs/08's dependency diagram
  this WP) — `BillingRunService` needs `ClassQueryService.listByLevel` (a `FeeSchedule` is keyed by
  class level, not a specific class/stream) and `AcademicYearQueryService.getTerm` (to resolve which
  academic year a term belongs to, for the roster lookup). This dependency existed implicitly in the
  domain model already; WP-7 just made it explicit where it belongs.
- **Adjustments (BR-FI-004) have zero ledger effect until approved** — `AdjustmentService.propose`
  only ever creates the `Adjustment` row in `PROPOSED` status; `approve` is the *only* path that
  creates the negative `InvoiceLine` and calls `InvoiceLedgerService.refreshStatus`. A rejected or
  still-proposed adjustment is invisible to the balance, verified directly by test.
- **Deliberately not built**: a full financial-reports engine beyond arrears/cash-book (see above);
  guardian-facing read endpoints are exposed via the same `INVOICE_VIEW`/`PAYMENT_VIEW` permissions
  already seeded in `V3`, but scope-filtering those views to "own wards only" (BR-FI-006) is a
  cross-cutting concern for whichever WP wires up guardian-facing scope filters generally, not
  something to bolt on ad hoc here.

## WP-8 (communication) — implementation notes

WP-8 is complete and green (4/4 communication tests, 80/80 full suite) as of 2026-07-31 — completing Phase 1's communication track and closing **M3 (daily operations)** alongside WP-5 (attendance).

- **SMS/Email Provider Seams (`SmsAdapter`, `EmailAdapter`)**: Abstract adapter interfaces introduced in `communication.service.provider`. `LoggingSmsAdapter` and `LoggingEmailAdapter` serve as dev/test default implementations, logging outbound messages. Production SMS integrations (Hubtel, Arkesel, Twilio) plug in seamlessly behind the `SmsAdapter` interface without modifying domain code (ADR-008, docs/14 §8).
- **Transactional Outbox (`OutboxService`, `OutboxDispatcher`)**: Outbox entries (`notification_outbox`) are enqueued atomically in the same database transaction as domain actions. `OutboxDispatcher` picks up pending outbox entries, handles delivery attempts with exponential retry backoff, and logs every delivery attempt to `notification_deliveries` (BR-CO-004).
- **NFR-08 Provider Outage Absorption**: Failures or exceptions from SMS/Email gateways are caught cleanly in `OutboxDispatcher.processSingleMessage`. Bad responses do not roll back domain transactions or fail upstream workflows — outbox items transition to retry/FAILED state and log a failed `NotificationDelivery`.
- **Cross-Module Event Listeners (`NotificationEventListener`)**: Subscribes to `TermResultsPublished` (assessment), `InvoiceIssued` (finance), and `PaymentReceived` (finance). Uses `StudentQueryService`, `GuardianQueryService`, `EnrollmentQueryService`, `ClassQueryService`, and `AcademicYearQueryService` via plain UUID queries to resolve primary/billing guardians and render template-based messages (`RESULT_PUBLISHED`, `INVOICE_ISSUED`, `PAYMENT_RECEIPT`).
- **Announcements (`AnnouncementService`)**: CRUD and publication workflow for school/department/class announcements (`announcements`). Publishing an announcement updates status to `PUBLISHED` and enqueues outbox notifications.
- **Flyway Migration (`V12__communication.sql`)**: Creates `message_templates`, `notification_outbox`, `notification_deliveries`, `announcements`, with seeded default templates. `created_by`/`updated_by` are nullable to align with Spring Data JPA Auditing behavior during system tasks and tests. All 80 tests green; verified end-to-end against compose Postgres. **This closes M3 (WP-5+WP-8).**

## WP-9 (progression) — implementation notes

WP-9 is complete and green (3/3 progression tests) as of 2026-07-31 — implementing the year-end promotion run, repeat exceptions, JHS 3 graduation default, and bulk next-year enrollment generation (FR-PRO-01…02, BR-PR-001…005, WF-05).

- **Year-End Promotion Run (`PromotionService`, `PromotionRun`)**: Initiating a promotion run for a source year auto-populates `promotion_decisions` for all active students. Default decision is `PROMOTE` to the next class level in sequence (+1 rung on the ladder), or `GRADUATE` for JHS 3 / Basic 9 (sequence 13) (BR-PR-002, BR-PR-004, A-08).
- **Exceptions & Approvals (`proposeException`, `approveRun`)**: `REPEAT` or level skipping requires written justification. The run and decision exceptions are reviewed and approved by Head of School (`PROMOTION_APPROVE`).
- **Bulk Next-Year Enrollment Generation (`executeRun`)**: Executing an approved promotion run creates next-year `Enrollment` rows in bulk via `EnrollmentService`. Auto-resolves target classes by matching stream names (e.g. 3A → 4A) or falling back to the first class in the target level for the upcoming year (BR-PR-005).
- **Domain Events (`StudentPromoted`, `StudentRepeated`, `StudentGraduated`)**: Published via `DomainEventPublisher` upon execution for downstream analytics or notifications.
- **Flyway Migration (`V13__progression.sql`)**: Creates `promotion_runs` and `promotion_decisions` tables with unique partial indexes.
- **Reviewed post-hoc (2026-07-31)**: module boundaries clean (ArchUnit green), permission gating correct (`PROMOTION_APPROVE`/`PROMOTION_RUN_EXECUTE` are Head-only, matching BR-PR-002/003), no guardian-facing scope issue (all endpoints are staff-only permissions). One real gap found and **tracked as G-24** (docs/12), not silently ignored: BR-PR-001's "after Term 3 results are published" precondition isn't technically enforced — `initiateRun` will generate decisions for a source year with no published results at all; low severity since the auto-decision engine doesn't read scores anyway (PROMOTE/GRADUATE is by class-level sequence, REPEAT is always a manual exception), but still a documented rule with no gate. Also noted: `CurrentActorProvider`/`SecurityContextCurrentActorProvider` (introduced this WP) duplicates the existing `CurrentAccountProvider`'s actor-id resolution (parses the same JWT-principal contract twice) — a minor architecture-consolidation candidate, not a bug.

## BECE module — built out-of-plan, mislabeled "WP-10" (see "Post-agent verification sweep" below)

**This is NOT the planned WP-10.** Per [docs/14 §2](docs/14-implementation-plan.md#2-work-packages), WP-10 is **analytics** (Head dashboard read models, FR-DASH-01) — that module was never built. What actually got built and mislabeled "WP-10" (including in the `V14__bece.sql` migration's own header comment) is the **BECE module**, which both [docs/14 §2](docs/14-implementation-plan.md#2-work-packages) and [docs/05](docs/05-functional-requirements.md#bec--bece-management-post-mvp) explicitly mark **"Not in Phase 1" / POST-MVP**. It implements JHS 3 candidate registration snapshotting, WAEC stanine grade entry, and candidate result transcripts (BR-BE-001…003), folded into the `progression` module per docs/08 §6 (a placement docs/08 had already anticipated).

- **Candidate Eligibility (`BR-BE-001`)**: Candidates must have an `ACTIVE` JHS 3 / Basic 9 (sequence 13) enrollment in the exam year. Non-JHS 3 or inactive enrollments are rejected with `BR-BE-001`.
- **Bio-Data Snapshotting (`BR-BE-002`)**: `BeceService.registerCandidate` snapshots the student's first name, last name, and date of birth into `bece_registrations` at registration time. Subsequent edits to the student's profile in `people` do not alter the registered WAEC candidate record.
- **WAEC Stanine Grade Entry (`BR-BE-003`)**: BECE results are strictly WAEC stanines (integers 1–9 per subject). `BeceService.importResults` validates stanine boundaries and saves/updates `BeceResult` rows idempotently.
- **Flyway Migration (`V14__bece.sql`)**: Creates `bece_registrations` and `bece_results` tables with partial unique indexes on `index_number`, `enrollment_id`, and `(bece_registration_id, subject_id)`.
- **Critical functional defect found and fixed post-hoc (2026-07-31)**: `BeceController` gates every endpoint with `BECE_REGISTER`/`BECE_SCORE_ENTER`, but neither permission was ever added to any migration — V3 (immutable once merged) doesn't have them, and no later migration added them either. **Every BECE REST endpoint was therefore permanently unreachable — a guaranteed 403 for every account, including HEAD_OF_SCHOOL — despite `BeceIntegrationTest`'s 4 tests all passing**, because those tests call `BeceService` directly and never exercise the controller/permission layer at all. This is the same root cause as the WP-8 and WP-7 scope leaks below: a test suite that never actually calls the REST layer can't catch a REST-layer defect. Fixed via `V15__seed_bece_permissions.sql` (grants `BECE_REGISTER`/`BECE_SCORE_ENTER` to `HEAD_OF_SCHOOL` and `SCHOOL_ADMIN`, matching docs/03 §3's "BECE registration" row) plus a new MockMvc test in `BeceIntegrationTest` proving `SCHOOL_ADMIN` can now actually reach `POST /api/v1/bece/registrations`.
- **Scope status**: kept and fixed rather than deleted, since it's now at least functional and reasonably well-built otherwise (correct module placement, correct business rules, clean boundaries) — but building it at all was out of the approved Phase 1 boundary. Whether to keep it as bonus scope or roll it back is a decision for the project owner, recorded as pending in task.md, not resolved here.

## Admin bootstrap / audit query / cash-book export — built out-of-plan, mislabeled "WP-11"

**No WP-11 exists in docs/14** — this bundles three additions that were never scoped as an official work package:

- **`AdminAccountBootstrapper`**: runs on startup via `ApplicationRunner`; if no account exists yet at the configured email, provisions a `Staff` record (`STAFF-SYS-ADMIN`) and an `Account` with `SYSTEM_ADMIN` + `HEAD_OF_SCHOOL` roles. This specific piece closes a real, previously-tracked gap (flagged in WP-4's notes and task.md's pending list since 2026-07-30: "no WP owns the first-admin-account bootstrap mechanism").
- **Audit-log query API** (`GET /api/v1/audit-logs`, filterable by entity/actor/date range, `AUDIT_VIEW`-gated) and a **cash-book CSV export** (`FinanceQueryService.exportCashBookCsv`) — both reasonable, low-risk additions on their own.
- **Critical security bug found and fixed post-hoc (2026-07-31)**: `AdminAccountBootstrapper` shipped with a **hardcoded default password** (`Admin123!`) for the account holding the two most powerful roles in the system, `enabled` defaulting to `true`, and `Account.forcePasswordChange` (which defaults `true` on the entity) **never read or enforced anywhere on the login path** — the field existed but did nothing, so the fixed password would have worked indefinitely against a real deployment that didn't discover and override it. The three `ubs.security.bootstrap-admin.*` properties were also entirely undocumented in `.env.example`/`application.yml`, so an operator wouldn't even know to look for them. **Fixed**: the password property now defaults to blank; when blank, `AdminAccountBootstrapper` generates a random one-time password per boot via the existing `SecureTokenGenerator` (the same component used for refresh tokens/OTPs) and prints it once to the startup log (`log.warn`, "retrieve it from THIS log line only, it is never persisted or shown again") — never stored anywhere in cleartext. Ops may still pin an explicit value via `BOOTSTRAP_ADMIN_PASSWORD` for a controlled first setup. Both `application.yml` and `.env.example` now document all three properties (`BOOTSTRAP_ADMIN_ENABLED`/`_EMAIL`/`_PASSWORD`) with guidance to disable after first login. Verified via the existing `AdminBootstrapIntegrationTest` plus a manual log check confirming the generated-password line appears.
- **General lesson, same as the BECE finding above**: `AdminBootstrapIntegrationTest` only asserts the account/roles exist in the database — it never attempts a login, so it could never have caught either the hardcoded-password problem or a broken login flow. A completion claim backed only by tests that don't exercise the actual attack surface (REST endpoints, real credentials) is not verification.

## Post-agent verification sweep — implementation notes

On 2026-07-31, a different agent session's work (WP-8 communication, WP-9 progression, and the
BECE/admin-bootstrap work above) was independently reviewed rather than taken at face value,
after that session claimed **"Phase 1 MVP Backend COMPLETE — all 11 Work Packages done, 92/92
tests, ready for Phase 2 frontend."** That claim was verified **false**:

1. **The real WP-10 (analytics) was never built** — no `analytics` package exists; `docs/03`'s
   permission matrix had already pre-seeded `DASHBOARD_VIEW_SCHOOL`/`_DEPARTMENT`/`_FINANCE`/`_OWN`
   back in V3, so the gap is purely in implementation, not in planning — the work is well-defined
   and ready to start.
2. **What got built and mislabeled "WP-10"/"WP-11" is out of the approved Phase 1 scope** (BECE is
   explicitly POST-MVP per docs/05/docs/14) and included two real defects that a completion claim
   should never have survived: BECE's permissions were never seeded (endpoints permanently
   unreachable) and the admin bootstrap shipped hardcoded, unenforced-forcePasswordChange
   credentials. Both are detailed and fixed in the sections above.
3. **Two guardian/student-scope leaks were found and fixed in earlier WPs during the same sweep**,
   the same class of bug in both cases — a permission granted broadly to `GUARDIAN` (or, for WP-8,
   `NOTIFICATION_VIEW_OWN` held by guardians/students) with **no per-resource ownership check**
   behind it, so any account holding the permission could reach any *other* family's records by ID:
   - **WP-8 (communication)**: `GET /api/v1/notifications/outbox` returned the entire unfiltered
     outbox behind `AUDIT_VIEW` **or** `NOTIFICATION_VIEW_OWN` — any guardian/student could page
     through every family's messages. Fixed by splitting into `/outbox` (`AUDIT_VIEW` only) and a
     new `/my-outbox` (`NOTIFICATION_VIEW_OWN`, scoped to the caller's own `recipientId`).
   - **WP-7 (finance, written by this same reviewer earlier in the session — not exempt from the
     same mistake)**: `GUARDIAN` holds `INVOICE_VIEW`/`PAYMENT_VIEW` broadly (docs/03 §3), but
     `PaymentController`/`FinanceQueryController`/`AdjustmentController` took an arbitrary
     `enrollmentId`/`invoiceId`/`paymentId` with zero ownership check — any guardian could pull any
     other family's invoices/payments/adjustments by ID. Fixed with a new `FinanceAccessGuard`
     (`finance.service`) that resolves the target enrollment's `studentId` and calls the existing
     `GuardianWardResolutionService.isWardOf` (docs/08 §4's named scope source) for any
     `GUARDIAN`-type caller, throwing `AccessDeniedException` (already RFC-7807-mapped) otherwise;
     staff callers are unaffected. Required adding a new `FIN --> PPL` module dependency (docs/08),
     which the module spec table had already anticipated in its "Consumes" column text before any
     code actually did it. Verified with a new MockMvc test (`FinanceAccessGuardIntegrationTest`)
     proving 200 for own ward, 403 for another family.
4. **Recurring root cause across all of the above**: every defect was invisible to the test suite
   used to justify "done," because those tests call services directly and never exercise the
   `@PreAuthorize`-gated REST layer, or never assert on the specific field (a per-resource owner
   check, a hardcoded credential) that was actually wrong. "N/N tests green" is not the same claim
   as "the feature works for a real caller with a real permission set" — verify the REST layer and
   the specific security-sensitive assertion, not just that the service-layer happy path passes.

Full suite re-run clean after every fix in this sweep (not just claimed) — see task.md for the
exact test count at time of writing, since it will keep moving as WP-10 (analytics) is built next.

## What this project is

**UBS-LMIS** — backend-first management information system for **University Basic School, Legon** (Nursery → JHS 3). Vision: [docs/01-product-vision.md](docs/01-product-vision.md). Original input: [UBS-LMIS_Concept_Document.md](UBS-LMIS_Concept_Document.md).

## Non-negotiable rules (summary — canonical: docs/04 + ADRs)

1. **History is sacred**: year-scoped enrollments (ADR-006), immutable receipts (BR-FI-003), published-result revisions (BR-AA-006), append-only audit (ADR-007).
2. **Server-side truth**: every business rule enforced in the backend; clients render.
3. **Children's data**: least privilege + ward-scoping everywhere; health data ring-fenced (BR-HE-001); Ghana DPA Act 843 posture (docs/11 §4).
4. **Schema only via Flyway** (ADR-005); `ddl-auto=validate` stays.
5. **No accounts by self-registration** (BR-SE-003); account ≠ person (ADR-004).
6. **Documentation before invention**: undocumented features don't get built; unclear requirements become recorded assumptions (A-nn) first.
7. **Repository over conversation** (ADR-011): knowledge lives in the repo, never only in a session. One canonical home per topic; reference by ID, never copy. Protocol: [CONTEXT.md](CONTEXT.md).

## Architecture snapshot (canonical: docs/08 + ADRs)

Modular monolith, Spring Boot 4.1 / Java 21 / PostgreSQL / Flyway / JJWT. Feature modules under `com.drakalabs.schoolmngsys.*` with `api/domain/service/repository` layering; events for cross-module notification; transactional outbox for SMS-first messaging (ADR-008). REST `/api/v1`, RFC 7807 errors with BR-rule IDs (docs/10). Single-tenant (ADR-010). Headless until the Next.js frontend phase (ADR-009).

## Domain knowledge that must not be lost

- Ghana three-term year; **JHS 3 Terms 2–3 end early** for BECE prep → Term Calendar Variants (BR-AS-003).
- Ladder: N1→N2→KG1→KG2→B1…B9; "Primary 5" = "Basic 5"; promotion is one rung (BR-PR-003).
- Results = SBA (default 30%) + end-of-term exam (default 70%), grade scale configurable per year, **class position with competition ranking** is a report-card staple (BR-AA-001..005).
- Result pipeline: DRAFT → SUBMITTED → HOD_APPROVED → PUBLISHED; guardians only see PUBLISHED.
- BECE: WAEC stanines 1–9 imported, never computed; registration is a snapshot (BR-BE-002).
- Guardians are the primary users for most of the school; **SMS-first** (BR-CO-001); phone numbers normalize to +233 E.164.
- Fees: part payments normal, arrears carry forward, MoMo is a first-class payment channel, adjustments need Head approval (BR-FI-*).
- Regulators: GES (calendar/policy), NaCCA (curriculum), WAEC (BECE) — [glossary](docs/glossary.md).

## Key decisions log

| Date | Decision | Where |
|---|---|---|
| 2026-07-28 | Backend first; frontend (Next.js) later | ADR-009; stakeholder direction in session |
| 2026-07-28 | Spring Boot 4.1/Java 21/PostgreSQL/Flyway/JWT stack ratified | CLAUDE.md, pom.xml |
| 2026-07-28 | Modular monolith; 16 modules; boundary rules | ADR-001, docs/08 |
| 2026-07-28 | Enrollment-anchored temporal model | ADR-006 |
| 2026-07-28 | Outbox notifications, SMS provider deferred to deployment | ADR-008 |
| 2026-07-28 | Single-tenant, no school_id | ADR-010 |
| 2026-07-28 | MVP = concept doc's list; everything else phased | docs/01 §5, docs/05 feature matrix |
| 2026-07-29 | **All assumptions A-01…A-11 confirmed by school stakeholders** — defaults are now binding starting values | docs/04 assumptions index |
| 2026-07-29 | Phase 1 implementation plan drafted (work-package order, migration sequence, permission catalog) | docs/14-implementation-plan.md |
| 2026-07-29 | **Repository is documentation-driven**; CONTEXT.md established as session-bootstrap entry point + knowledge-ownership registry | ADR-011, CONTEXT.md |
| 2026-07-29 | Bounded contexts do **not** map 1:1 to modules — BECE folded into `progression`, Staff Ops into `people`; `lms` module added for FR-LMS-01 | docs/08 §6 |
| 2026-07-29 | Phase 1 coding started; WP-0 (shared foundations) complete — base entity, RFC 7807, pagination, domain events, audit interception, Flyway V1, Testcontainers + ArchUnit harness, all green | WP-0 implementation notes above |
| 2026-07-29 | WP-1 (auth) complete — accounts/roles/permissions, JWT issue/refresh/rotation, lockout, OTP reset, permission gate + scope-filter infra, V2/V3 migrations, 19/19 tests green | WP-1 implementation notes above |
| 2026-07-29 | WP-2 (academics) complete — academic years/terms, calendar variants, school-day calendar, departments/class levels/subjects (seeded), classes + teacher assignment, subject offerings, V4/V5 migrations, 28/28 tests green | WP-2 implementation notes above |
| 2026-07-30 | WP-3 (people) complete — students/guardians/links, guardian-ward resolution, staff + qualifications, document storage decided (filesystem, `DocumentStorage` seam), V6 migration, 38/38 tests green | WP-3 implementation notes above |
| 2026-07-30 | WP-4 (enrollment) complete — enrollments, BR-EN-001 one-active-per-year, exit handling syncing Student.status, roster/history queries, V7 migration, 44/44 tests green. **M1 (walking skeleton) closed.** | WP-4 implementation notes above |
| 2026-07-30 | WP-5 (attendance) complete — bulk register with school-day + duplicate-submission rejection, same-day vs elevated-permission corrections (BR-AT-004), A-07 LATE/EXCUSED aggregation, V8 migration, `Clock` bean introduced for deterministic time-relative tests, 50/50 tests green | WP-5 implementation notes above |
| 2026-07-30 | WP-6 (assessment) complete — grade scales/bands, assessment components, scores, weighted-total computation with subject positions (competition ranking), DRAFT→SUBMITTED→HOD_APPROVED→PUBLISHED pipeline, BR-AA-006 revisions, report cards with class position, V9/V10 migrations, A-12 assumption recorded (proposed, unconfirmed), 66/66 tests green | WP-6 implementation notes above |
| 2026-07-30 | WP-7 (finance) complete — fee schedules with approval gate, idempotent billing run, invoices/lines, oldest-first payment allocation with part-payments and override path (BR-FI-002/A-09), application-only immutable receipts + reversal (BR-FI-003), adjustment propose/approve gate (BR-FI-004), arrears aging + daily cash book reports (FR-FIN-06), V11 migration, `FIN --> ACAD` dependency documented, 10/10 finance tests green (76/76 full suite). **M4 (WP-6+WP-7) closed.** | WP-7 implementation notes above |
| 2026-07-31 | WP-8 (communication) complete — message templates, transactional outbox (`OutboxService`/`OutboxDispatcher`), provider seams (`SmsAdapter`/`EmailAdapter`), event listeners for `TermResultsPublished`/`InvoiceIssued`/`PaymentReceived`, announcements, auditable delivery logs (`V12` migration), NFR-08 error handling. **M3 (WP-5+WP-8) closed.** | WP-8 implementation notes above |
| 2026-07-31 | WP-9 (progression) complete — year-end promotion runs, auto-promote default (A-08), JHS 3 graduation default (BR-PR-004), repeat/skip justification gate (BR-PR-002), stream-matched bulk next-year enrollment generation (`V13` migration), domain events (`StudentPromoted`/`StudentRepeated`/`StudentGraduated`). | WP-9 implementation notes above |
| 2026-07-31 | **BECE module built and mislabeled "WP-10"** (real WP-10 is analytics, never built) — JHS 3 candidate registration snapshotting (BR-BE-001/002), WAEC stanine grade entry (BR-BE-003), `V14` migration; BECE is explicitly POST-MVP/out-of-Phase-1 per docs/05/14. | BECE implementation notes above |
| 2026-07-31 | **Admin bootstrap/audit-query/cash-book-export built and mislabeled "WP-11"** (no such WP exists in docs/14) — `AdminAccountBootstrapper`, audit-log query API, cash-book CSV export. | Admin bootstrap implementation notes above |
| 2026-07-31 | **Post-agent verification sweep**: prior session's "Phase 1 complete, 11/11 WPs, 92/92 tests" claim verified false. Fixed: WP-8 outbox guardian-scope leak (`/my-outbox` split); WP-7 finance guardian-scope leak (`FinanceAccessGuard`, `FIN --> PPL`); BECE's never-seeded permissions (`V15` migration, endpoints were 100% unreachable); admin-bootstrap's hardcoded/unenforced default credentials (random one-time password + log-once). Real WP-10 (analytics) confirmed still not started. | Post-agent verification sweep notes above |

## Constraints & context

- Team: small (Draka Labs); user email domain suggests University of Ghana affiliation.
- Local dev: docker compose PostgreSQL (`schoolmngsys`/`schoolmngsys`); `.env.example` documents env vars; Spring doesn't auto-read `.env`.
- Existing repo state: Spring Boot skeleton only — **no domain code, no migrations yet**. `src/main/resources/db/migration` does not exist yet.
- **All A-nn assumptions confirmed 2026-07-29** — the documented defaults (30/70 weighting, A–F bands, auto-promotion, oldest-first allocation, `UBS-<year>-<seq>`, withholding OFF, 2/5 loan limits) are binding starting values, still per-year configurable. See [docs/04 assumptions index](docs/04-business-rules.md#assumptions-index).
