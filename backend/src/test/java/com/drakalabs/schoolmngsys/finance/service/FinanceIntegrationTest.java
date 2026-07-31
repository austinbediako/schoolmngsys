package com.drakalabs.schoolmngsys.finance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.drakalabs.schoolmngsys.AbstractIntegrationTest;
import com.drakalabs.schoolmngsys.academics.repository.ClassLevelRepository;
import com.drakalabs.schoolmngsys.academics.service.AcademicYearQueryService;
import com.drakalabs.schoolmngsys.academics.service.AcademicYearService;
import com.drakalabs.schoolmngsys.academics.service.AcademicYearView;
import com.drakalabs.schoolmngsys.academics.service.ClassService;
import com.drakalabs.schoolmngsys.academics.service.ClassView;
import com.drakalabs.schoolmngsys.academics.service.TermSpec;
import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentService;
import com.drakalabs.schoolmngsys.finance.domain.AdjustmentStatus;
import com.drakalabs.schoolmngsys.finance.domain.AdjustmentType;
import com.drakalabs.schoolmngsys.finance.domain.InvoiceStatus;
import com.drakalabs.schoolmngsys.finance.domain.PaymentChannel;
import com.drakalabs.schoolmngsys.people.domain.Gender;
import com.drakalabs.schoolmngsys.people.domain.RelationshipType;
import com.drakalabs.schoolmngsys.people.service.GuardianLinkSpec;
import com.drakalabs.schoolmngsys.people.service.GuardianService;
import com.drakalabs.schoolmngsys.people.service.GuardianView;
import com.drakalabs.schoolmngsys.people.service.StudentService;
import com.drakalabs.schoolmngsys.people.service.StudentView;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * WP-7 (docs/14 §5): fee schedule approval gate (BR-FI-001), billing-run idempotence and arrears
 * carry-forward (BR-FI-005/FR-FIN-02), oldest-first allocation incl. part payments and the
 * override path (BR-FI-002/A-09), receipt/payment immutability and reversal correctness
 * (BR-FI-003), and the adjustment propose/approve gate (BR-FI-004).
 */
class FinanceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private FeeScheduleService feeScheduleService;

    @Autowired
    private BillingRunService billingRunService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AdjustmentService adjustmentService;

    @Autowired
    private FinanceQueryService financeQueryService;

    @Autowired
    private AcademicYearService academicYearService;

    @Autowired
    private AcademicYearQueryService academicYearQueryService;

    @Autowired
    private ClassService classService;

    @Autowired
    private ClassLevelRepository classLevelRepository;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private GuardianService guardianService;

    private int phoneCounter = 6000000;

    private AcademicYearView newYear() {
        return academicYearService.createYear(
                "Y-" + UUID.randomUUID(),
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2027, 8, 1),
                List.of(
                        new TermSpec(1, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 15), 70),
                        new TermSpec(2, LocalDate.of(2027, 1, 5), LocalDate.of(2027, 4, 4), 60),
                        new TermSpec(3, LocalDate.of(2027, 4, 25), LocalDate.of(2027, 8, 1), 65)));
    }

    private UUID termIdOf(AcademicYearView year, int termNumber) {
        return academicYearQueryService.listTerms(year.id()).stream()
                .filter(term -> term.termNumber() == termNumber)
                .findFirst()
                .orElseThrow()
                .id();
    }

    private UUID classLevelId(String code) {
        return classLevelRepository.findByCodeAndArchivedAtIsNull(code).orElseThrow().getId();
    }

    private UUID newEnrollment(AcademicYearView year, UUID classId) {
        GuardianView guardian = guardianService.createGuardian("Abena", "Owusu", "+23322" + (phoneCounter++), null, null, null);
        StudentView student = studentService.createStudent(
                "Yaw",
                "Owusu",
                null,
                LocalDate.of(2016, 3, 1),
                Gender.MALE,
                LocalDate.of(2026, 9, 1),
                List.of(new GuardianLinkSpec(guardian.id(), RelationshipType.FATHER, true, true, true, true)));
        return enrollmentService.enroll(student.id(), classId, year.id(), null).id();
    }

    private FeeScheduleView approvedSchedule(UUID classLevelId, UUID termId, BigDecimal amount) {
        FeeScheduleView schedule =
                feeScheduleService.create(classLevelId, termId, List.of(new FeeItemSpec("Tuition", amount, true)));
        return feeScheduleService.approve(schedule.id());
    }

    @Test
    void billingCannotRunAgainstAnUnapprovedSchedule() {
        AcademicYearView year = newYear();
        UUID termId = termIdOf(year, 1);
        UUID classLevelId = classLevelId("B4");
        feeScheduleService.create(classLevelId, termId, List.of(new FeeItemSpec("Tuition", new BigDecimal("500.00"), true)));

        assertThatThrownBy(() -> billingRunService.runBilling(classLevelId, termId))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-FI-001"));
    }

    @Test
    void billingRunInvoicesEveryActiveEnrollmentAndIsIdempotentOnRerun() {
        AcademicYearView year = newYear();
        UUID termId = termIdOf(year, 1);
        UUID classLevelId = classLevelId("B5");
        ClassView schoolClass = classService.createClass("B5", "A-" + UUID.randomUUID(), 30);
        approvedSchedule(classLevelId, termId, new BigDecimal("500.00"));

        UUID enrollmentA = newEnrollment(year, schoolClass.id());
        UUID enrollmentB = newEnrollment(year, schoolClass.id());

        List<InvoiceView> firstRun = billingRunService.runBilling(classLevelId, termId);
        assertThat(firstRun).hasSize(2);
        assertThat(firstRun).allSatisfy(invoice -> assertThat(invoice.totalAmount()).isEqualByComparingTo("500.00"));

        List<InvoiceView> secondRun = billingRunService.runBilling(classLevelId, termId);
        assertThat(secondRun).isEmpty(); // already billed for this term -> idempotent skip

        assertThat(financeQueryService.invoiceHistory(enrollmentA)).hasSize(1);
        assertThat(financeQueryService.invoiceHistory(enrollmentB)).hasSize(1);
    }

    @Test
    void arrearsFromAnOlderTermStayOpenAndAreNotDuplicatedOntoTheNewInvoice() {
        AcademicYearView year = newYear();
        UUID term1 = termIdOf(year, 1);
        UUID term2 = termIdOf(year, 2);
        UUID classLevelId = classLevelId("B6");
        ClassView schoolClass = classService.createClass("B6", "A-" + UUID.randomUUID(), 30);
        approvedSchedule(classLevelId, term1, new BigDecimal("500.00"));
        approvedSchedule(classLevelId, term2, new BigDecimal("500.00"));

        UUID enrollmentId = newEnrollment(year, schoolClass.id());

        List<InvoiceView> term1Invoices = billingRunService.runBilling(classLevelId, term1);
        UUID term1InvoiceId = term1Invoices.get(0).id();
        // Term 1 invoice is left entirely unpaid -> arrears.
        billingRunService.runBilling(classLevelId, term2);

        List<InvoiceView> history = financeQueryService.invoiceHistory(enrollmentId);
        assertThat(history).hasSize(2); // one invoice per term, arrears is NOT duplicated as a line item
        assertThat(history).allSatisfy(invoice -> assertThat(invoice.totalAmount()).isEqualByComparingTo("500.00"));

        // Paying only 500 (one term's worth) settles the OLDER (term 1) invoice first.
        paymentService.recordPayment(enrollmentId, new BigDecimal("500.00"), PaymentChannel.CASH, null, null, null);

        assertThat(financeQueryService.getInvoice(term1InvoiceId).status()).isEqualTo(InvoiceStatus.PAID);
        List<ArrearsEntry> arrears = financeQueryService.arrears(classLevelId, year.id());
        assertThat(arrears).hasSize(1); // only term 2's invoice remains outstanding
        assertThat(arrears.get(0).balance()).isEqualByComparingTo("500.00");
    }

    @Test
    void aPartPaymentAllocatesOldestFirstAcrossTwoOpenInvoices() {
        AcademicYearView year = newYear();
        UUID term1 = termIdOf(year, 1);
        UUID term2 = termIdOf(year, 2);
        UUID classLevelId = classLevelId("B7");
        ClassView schoolClass = classService.createClass("B7", "A-" + UUID.randomUUID(), 30);
        approvedSchedule(classLevelId, term1, new BigDecimal("300.00"));
        approvedSchedule(classLevelId, term2, new BigDecimal("300.00"));

        UUID enrollmentId = newEnrollment(year, schoolClass.id());
        List<InvoiceView> term1Invoices = billingRunService.runBilling(classLevelId, term1);
        UUID term1InvoiceId = term1Invoices.get(0).id();
        List<InvoiceView> term2Invoices = billingRunService.runBilling(classLevelId, term2);
        UUID term2InvoiceId = term2Invoices.get(0).id();

        // 400 covers term 1 (300) in full and leaves 100 applied as a part payment on term 2.
        PaymentView payment =
                paymentService.recordPayment(enrollmentId, new BigDecimal("400.00"), PaymentChannel.MOMO, "MOMO-REF-1", null, null);

        assertThat(payment.allocations()).hasSize(2);
        assertThat(financeQueryService.getInvoice(term1InvoiceId).status()).isEqualTo(InvoiceStatus.PAID);
        assertThat(financeQueryService.getInvoice(term1InvoiceId).balance()).isEqualByComparingTo("0.00");
        assertThat(financeQueryService.getInvoice(term2InvoiceId).status()).isEqualTo(InvoiceStatus.PART_PAID);
        assertThat(financeQueryService.getInvoice(term2InvoiceId).balance()).isEqualByComparingTo("200.00");
    }

    @Test
    void targetingASpecificInvoiceOutOfOrderRequiresAnOverrideReason() {
        AcademicYearView year = newYear();
        UUID termId = termIdOf(year, 1);
        UUID classLevelId = classLevelId("B8");
        ClassView schoolClass = classService.createClass("B8", "A-" + UUID.randomUUID(), 30);
        approvedSchedule(classLevelId, termId, new BigDecimal("500.00"));
        UUID enrollmentId = newEnrollment(year, schoolClass.id());
        UUID invoiceId = billingRunService.runBilling(classLevelId, termId).get(0).id();

        assertThatThrownBy(() -> paymentService.recordPayment(
                        enrollmentId, new BigDecimal("100.00"), PaymentChannel.CASH, null, invoiceId, null))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-FI-002"));

        PaymentView payment = paymentService.recordPayment(
                enrollmentId, new BigDecimal("100.00"), PaymentChannel.CASH, null, invoiceId, "Guardian requested this term specifically");
        assertThat(payment.allocations()).hasSize(1);
        assertThat(payment.allocations().get(0).invoiceId()).isEqualTo(invoiceId);
    }

    @Test
    void everyPostedPaymentGetsAUniqueImmutableReceiptNumber() {
        AcademicYearView year = newYear();
        UUID termId = termIdOf(year, 1);
        UUID classLevelId = classLevelId("B9");
        ClassView schoolClass = classService.createClass("B9", "A-" + UUID.randomUUID(), 30);
        approvedSchedule(classLevelId, termId, new BigDecimal("500.00"));
        UUID enrollmentId = newEnrollment(year, schoolClass.id());
        billingRunService.runBilling(classLevelId, termId);

        PaymentView first = paymentService.recordPayment(enrollmentId, new BigDecimal("200.00"), PaymentChannel.CASH, null, null, null);
        PaymentView second = paymentService.recordPayment(enrollmentId, new BigDecimal("100.00"), PaymentChannel.BANK, "REF-2", null, null);

        assertThat(first.receiptNumber()).isNotBlank();
        assertThat(second.receiptNumber()).isNotBlank().isNotEqualTo(first.receiptNumber());

        // Re-reading the posted payment reflects exactly what was recorded — nothing to update it via.
        PaymentView reread = paymentService.get(first.id());
        assertThat(reread.amount()).isEqualByComparingTo(first.amount());
        assertThat(reread.receiptNumber()).isEqualTo(first.receiptNumber());
        assertThat(reread.channel()).isEqualTo(PaymentChannel.CASH);
    }

    @Test
    void reversingAPaymentNetsTheInvoiceBalanceBackAndCannotBeReversedTwice() {
        AcademicYearView year = newYear();
        UUID termId = termIdOf(year, 1);
        UUID classLevelId = classLevelId("N1");
        ClassView schoolClass = classService.createClass("N1", "A-" + UUID.randomUUID(), 30);
        approvedSchedule(classLevelId, termId, new BigDecimal("500.00"));
        UUID enrollmentId = newEnrollment(year, schoolClass.id());
        UUID invoiceId = billingRunService.runBilling(classLevelId, termId).get(0).id();

        PaymentView payment = paymentService.recordPayment(enrollmentId, new BigDecimal("500.00"), PaymentChannel.CHEQUE, "CHQ-1", null, null);
        assertThat(financeQueryService.getInvoice(invoiceId).status()).isEqualTo(InvoiceStatus.PAID);

        assertThatThrownBy(() -> paymentService.reversePayment(payment.id(), " "))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-FI-003"));

        PaymentView reversal = paymentService.reversePayment(payment.id(), "Cheque bounced");
        assertThat(reversal.amount()).isEqualByComparingTo(new BigDecimal("-500.00"));
        assertThat(reversal.reversalOfPaymentId()).isEqualTo(payment.id());
        assertThat(financeQueryService.getInvoice(invoiceId).status()).isEqualTo(InvoiceStatus.ISSUED);
        assertThat(financeQueryService.getInvoice(invoiceId).balance()).isEqualByComparingTo("500.00");

        assertThat(paymentService.get(payment.id()).reversed()).isTrue();
        assertThatThrownBy(() -> paymentService.reversePayment(payment.id(), "Trying again"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-FI-003"));
        assertThatThrownBy(() -> paymentService.reversePayment(reversal.id(), "Reversing a reversal"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-FI-003"));
    }

    @Test
    void anAdjustmentHasNoEffectUntilItIsApprovedByTheHead() {
        AcademicYearView year = newYear();
        UUID termId = termIdOf(year, 1);
        UUID classLevelId = classLevelId("N2");
        ClassView schoolClass = classService.createClass("N2", "A-" + UUID.randomUUID(), 30);
        approvedSchedule(classLevelId, termId, new BigDecimal("500.00"));
        UUID enrollmentId = newEnrollment(year, schoolClass.id());
        UUID invoiceId = billingRunService.runBilling(classLevelId, termId).get(0).id();

        AdjustmentView proposed =
                adjustmentService.propose(invoiceId, AdjustmentType.SCHOLARSHIP, new BigDecimal("100.00"), "Merit scholarship");
        assertThat(proposed.status()).isEqualTo(AdjustmentStatus.PROPOSED);
        assertThat(financeQueryService.getInvoice(invoiceId).totalAmount()).isEqualByComparingTo("500.00"); // unaffected

        AdjustmentView approved = adjustmentService.approve(proposed.id());
        assertThat(approved.status()).isEqualTo(AdjustmentStatus.APPROVED);
        assertThat(financeQueryService.getInvoice(invoiceId).totalAmount()).isEqualByComparingTo("400.00");
        assertThat(financeQueryService.getInvoice(invoiceId).balance()).isEqualByComparingTo("400.00");

        assertThatThrownBy(() -> adjustmentService.approve(approved.id()))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-FI-004"));
    }

    @Test
    void aRejectedAdjustmentNeverTouchesTheLedger() {
        AcademicYearView year = newYear();
        UUID termId = termIdOf(year, 1);
        UUID classLevelId = classLevelId("KG1");
        ClassView schoolClass = classService.createClass("KG1", "A-" + UUID.randomUUID(), 30);
        approvedSchedule(classLevelId, termId, new BigDecimal("500.00"));
        UUID enrollmentId = newEnrollment(year, schoolClass.id());
        UUID invoiceId = billingRunService.runBilling(classLevelId, termId).get(0).id();

        AdjustmentView proposed = adjustmentService.propose(invoiceId, AdjustmentType.DISCOUNT, new BigDecimal("50.00"), "Sibling discount");
        AdjustmentView rejected = adjustmentService.reject(proposed.id());

        assertThat(rejected.status()).isEqualTo(AdjustmentStatus.REJECTED);
        assertThat(financeQueryService.getInvoice(invoiceId).totalAmount()).isEqualByComparingTo("500.00");
        assertThatThrownBy(() -> adjustmentService.approve(rejected.id())).isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void payingAnInvoiceThatDoesNotExistWhenTargeted() {
        AcademicYearView year = newYear();
        UUID classLevelId = classLevelId("KG2");
        ClassView schoolClass = classService.createClass("KG2", "A-" + UUID.randomUUID(), 30);
        UUID enrollmentId = newEnrollment(year, schoolClass.id());

        assertThatThrownBy(() -> paymentService.recordPayment(
                        enrollmentId, new BigDecimal("50.00"), PaymentChannel.CASH, null, UUID.randomUUID(), "override"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void exportCashBookCsvGeneratesValidCsvSummary() {
        AcademicYearView year = newYear();
        UUID classLevelId = classLevelId("N1");
        ClassView schoolClass = classService.createClass("N1", "A-" + UUID.randomUUID(), 30);
        approvedSchedule(classLevelId, termIdOf(year, 1), new BigDecimal("100.00"));
        UUID enrollmentId = newEnrollment(year, schoolClass.id());
        billingRunService.runBilling(classLevelId, termIdOf(year, 1));

        paymentService.recordPayment(enrollmentId, new BigDecimal("100.00"), PaymentChannel.CASH, "REC-EXPORT-1", null, null);

        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now().plusSeconds(3600);

        String csv = financeQueryService.exportCashBookCsv(from, to);
        assertThat(csv).contains("Receipt Number,Enrollment ID,Channel,Reference,Amount (GHS),Reversed");
        assertThat(csv).contains("100.00");
        assertThat(csv).contains("TOTAL");
    }
}
