package com.drakalabs.schoolmngsys.finance.service;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.drakalabs.schoolmngsys.AbstractIntegrationTest;
import com.drakalabs.schoolmngsys.academics.repository.ClassLevelRepository;
import com.drakalabs.schoolmngsys.academics.service.AcademicYearQueryService;
import com.drakalabs.schoolmngsys.academics.service.AcademicYearService;
import com.drakalabs.schoolmngsys.academics.service.AcademicYearView;
import com.drakalabs.schoolmngsys.academics.service.ClassService;
import com.drakalabs.schoolmngsys.academics.service.ClassView;
import com.drakalabs.schoolmngsys.academics.service.TermSpec;
import com.drakalabs.schoolmngsys.auth.service.AccountCreationResult;
import com.drakalabs.schoolmngsys.auth.service.AccountProvisioningService;
import com.drakalabs.schoolmngsys.auth.service.AuthTokens;
import com.drakalabs.schoolmngsys.auth.service.AuthenticationService;
import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentService;
import com.drakalabs.schoolmngsys.people.domain.Gender;
import com.drakalabs.schoolmngsys.people.domain.RelationshipType;
import com.drakalabs.schoolmngsys.people.service.GuardianLinkSpec;
import com.drakalabs.schoolmngsys.people.service.GuardianService;
import com.drakalabs.schoolmngsys.people.service.GuardianView;
import com.drakalabs.schoolmngsys.people.service.StudentService;
import com.drakalabs.schoolmngsys.people.service.StudentView;
import com.drakalabs.schoolmngsys.shared.security.PersonType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Regression test for the post-hoc WP-7 finding: GUARDIAN holds INVOICE_VIEW/PAYMENT_VIEW broadly
 * (docs/03), so the permission alone can't stop a guardian reaching another family's financial
 * records — {@link FinanceAccessGuard} closes that with a ward-ownership check (BR-FI-006).
 */
@AutoConfigureMockMvc
class FinanceAccessGuardIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountProvisioningService accountProvisioningService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private FeeScheduleService feeScheduleService;

    @Autowired
    private BillingRunService billingRunService;

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

    private int counter = 9000000;

    @Test
    void aGuardianMayViewTheirOwnWardsInvoiceButNotAnotherFamilys() throws Exception {
        AcademicYearView year = academicYearService.createYear(
                "Y-" + UUID.randomUUID(),
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2027, 8, 1),
                List.of(
                        new TermSpec(1, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 15), 70),
                        new TermSpec(2, LocalDate.of(2027, 1, 5), LocalDate.of(2027, 4, 4), 60),
                        new TermSpec(3, LocalDate.of(2027, 4, 25), LocalDate.of(2027, 8, 1), 65)));
        UUID termId = academicYearQueryService.listTerms(year.id()).get(0).id();
        UUID classLevelId = classLevelRepository.findByCodeAndArchivedAtIsNull("B2").orElseThrow().getId();
        ClassView schoolClass = classService.createClass("B2", "A-" + UUID.randomUUID().toString().substring(0, 6), 30);

        feeScheduleService.approve(
                feeScheduleService.create(classLevelId, termId, List.of(new FeeItemSpec("Tuition", new BigDecimal("300.00"), true))).id());

        GuardianView guardianA = guardianService.createGuardian("Efua", "Danso", "+23325" + (counter++), null, null, null);
        StudentView studentA = studentService.createStudent(
                "Nana", "Danso", null, LocalDate.of(2016, 2, 2), Gender.FEMALE, LocalDate.of(2026, 9, 1),
                List.of(new GuardianLinkSpec(guardianA.id(), RelationshipType.MOTHER, true, true, true, true)));
        UUID enrollmentA = enrollmentService.enroll(studentA.id(), schoolClass.id(), year.id(), null).id();

        GuardianView guardianB = guardianService.createGuardian("Kwesi", "Nti", "+23326" + (counter++), null, null, null);
        StudentView studentB = studentService.createStudent(
                "Esi", "Nti", null, LocalDate.of(2016, 3, 3), Gender.FEMALE, LocalDate.of(2026, 9, 1),
                List.of(new GuardianLinkSpec(guardianB.id(), RelationshipType.FATHER, true, true, true, true)));
        UUID enrollmentB = enrollmentService.enroll(studentB.id(), schoolClass.id(), year.id(), null).id();

        List<InvoiceView> invoices = billingRunService.runBilling(classLevelId, termId);
        UUID invoiceAId = invoices.stream().filter(inv -> inv.enrollmentId().equals(enrollmentA)).findFirst().orElseThrow().id();
        UUID invoiceBId = invoices.stream().filter(inv -> inv.enrollmentId().equals(enrollmentB)).findFirst().orElseThrow().id();

        String identifier = "guardianA.finance." + UUID.randomUUID().toString().substring(0, 8);
        AccountCreationResult account =
                accountProvisioningService.createAccount(PersonType.GUARDIAN, guardianA.id(), identifier, guardianA.phone(), null);
        accountProvisioningService.assignRole(account.account().id(), "GUARDIAN");
        AuthTokens tokens = authenticationService.login(identifier, account.temporaryPassword(), "127.0.0.1");

        mockMvc.perform(get("/api/v1/invoices/" + invoiceAId).header("Authorization", "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/invoices/" + invoiceBId).header("Authorization", "Bearer " + tokens.accessToken()))
                .andExpect(status().isForbidden());

        mockMvc.perform(
                        get("/api/v1/enrollments/" + enrollmentB + "/invoices")
                                .header("Authorization", "Bearer " + tokens.accessToken()))
                .andExpect(status().isForbidden());
    }
}
