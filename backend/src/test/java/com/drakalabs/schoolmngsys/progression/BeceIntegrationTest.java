package com.drakalabs.schoolmngsys.progression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.drakalabs.schoolmngsys.AbstractIntegrationTest;
import com.drakalabs.schoolmngsys.academics.service.AcademicYearService;
import com.drakalabs.schoolmngsys.academics.service.AcademicYearView;
import com.drakalabs.schoolmngsys.academics.service.ClassService;
import com.drakalabs.schoolmngsys.academics.service.ClassView;
import com.drakalabs.schoolmngsys.academics.service.SubjectQueryService;
import com.drakalabs.schoolmngsys.academics.service.SubjectView;
import com.drakalabs.schoolmngsys.academics.service.TermSpec;
import com.drakalabs.schoolmngsys.auth.service.AccountCreationResult;
import com.drakalabs.schoolmngsys.auth.service.AccountProvisioningService;
import com.drakalabs.schoolmngsys.auth.service.AuthTokens;
import com.drakalabs.schoolmngsys.auth.service.AuthenticationService;
import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentService;
import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentView;
import com.drakalabs.schoolmngsys.people.domain.Gender;
import com.drakalabs.schoolmngsys.people.domain.RelationshipType;
import com.drakalabs.schoolmngsys.people.service.GuardianLinkSpec;
import com.drakalabs.schoolmngsys.people.service.GuardianService;
import com.drakalabs.schoolmngsys.people.service.GuardianView;
import com.drakalabs.schoolmngsys.people.service.StudentService;
import com.drakalabs.schoolmngsys.people.service.StudentView;
import com.drakalabs.schoolmngsys.progression.service.BeceQueryService;
import com.drakalabs.schoolmngsys.progression.service.BeceRegistrationView;
import com.drakalabs.schoolmngsys.progression.service.BeceResultView;
import com.drakalabs.schoolmngsys.progression.service.BeceService;
import com.drakalabs.schoolmngsys.progression.service.BeceSubjectScoreSpec;
import com.drakalabs.schoolmngsys.shared.security.PersonType;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
public class BeceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private BeceService beceService;

    @Autowired
    private BeceQueryService beceQueryService;

    @Autowired
    private AcademicYearService academicYearService;

    @Autowired
    private ClassService classService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private GuardianService guardianService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private SubjectQueryService subjectQueryService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountProvisioningService accountProvisioningService;

    @Autowired
    private AuthenticationService authenticationService;

    private int counter = 9000000;
    private AcademicYearView year;
    private ClassView jhs3Class;
    private ClassView b1Class;

    @BeforeEach
    void setUp() {
        year = academicYearService.createYear(
                "BECE-YEAR-" + UUID.randomUUID().toString().substring(0, 4),
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2026, 7, 31),
                List.of(
                        new TermSpec(1, LocalDate.of(2025, 9, 1), LocalDate.of(2025, 12, 15), 70),
                        new TermSpec(2, LocalDate.of(2026, 1, 5), LocalDate.of(2026, 4, 4), 60),
                        new TermSpec(3, LocalDate.of(2026, 4, 25), LocalDate.of(2026, 7, 31), 65)
                )
        );
        jhs3Class = classService.createClass("B9", "A-" + UUID.randomUUID().toString().substring(0, 4), 30);
        b1Class = classService.createClass("B1", "A-" + UUID.randomUUID().toString().substring(0, 4), 30);
    }

    private StudentView createStudent(String firstName, String lastName) {
        GuardianView guardian = guardianService.createGuardian("Guardian", lastName, "+23320" + (counter++), null, null, null);

        return studentService.createStudent(
                firstName,
                lastName,
                null,
                LocalDate.of(2010, 5, 15),
                Gender.MALE,
                LocalDate.now(),
                List.of(new GuardianLinkSpec(guardian.id(), RelationshipType.FATHER, true, true, true, true))
        );
    }

    @Test
    void registeringNonJhs3StudentFails() {
        StudentView student = createStudent("Kofi", "Mensah");
        EnrollmentView enrollment = enrollmentService.enroll(student.id(), b1Class.id(), year.id(), null);

        assertThatThrownBy(() -> beceService.registerCandidate(enrollment.id(), "1002003001"))
                .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex -> assertThat(ex.getRuleId()).isEqualTo("BR-BE-001"));
    }

    @Test
    void registeringJhs3StudentSnapshotsBioData() {
        StudentView student = createStudent("Ama", "Serwaa");
        EnrollmentView enrollment = enrollmentService.enroll(student.id(), jhs3Class.id(), year.id(), null);

        BeceRegistrationView reg = beceService.registerCandidate(enrollment.id(), "1002003002");

        assertThat(reg.indexNumber()).isEqualTo("1002003002");
        assertThat(reg.snapshotFirstName()).isEqualTo("Ama");
        assertThat(reg.snapshotLastName()).isEqualTo("Serwaa");
        assertThat(reg.snapshotDob()).isEqualTo(LocalDate.of(2010, 5, 15));

        BeceRegistrationView retrieved = beceQueryService.getRegistration(reg.id());
        assertThat(retrieved.indexNumber()).isEqualTo("1002003002");
    }

    @Test
    void importingInvalidStanineGradeFails() {
        StudentView student = createStudent("Kwame", "Nkrumah");
        EnrollmentView enrollment = enrollmentService.enroll(student.id(), jhs3Class.id(), year.id(), null);
        BeceRegistrationView reg = beceService.registerCandidate(enrollment.id(), "1002003003");

        SubjectView math = subjectQueryService.list().get(0);

        assertThatThrownBy(() -> beceService.importResults(reg.id(), List.of(new BeceSubjectScoreSpec(math.id(), 0))))
                .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex -> assertThat(ex.getRuleId()).isEqualTo("BR-BE-003"));

        assertThatThrownBy(() -> beceService.importResults(reg.id(), List.of(new BeceSubjectScoreSpec(math.id(), 10))))
                .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex -> assertThat(ex.getRuleId()).isEqualTo("BR-BE-003"));
    }

    @Test
    void importingValidStanineGradesSucceedsIdempotently() {
        StudentView student = createStudent("Yaa", "Asantewaa");
        EnrollmentView enrollment = enrollmentService.enroll(student.id(), jhs3Class.id(), year.id(), null);
        BeceRegistrationView reg = beceService.registerCandidate(enrollment.id(), "1002003004");

        List<SubjectView> subjects = subjectQueryService.list();
        SubjectView math = subjects.get(0);
        SubjectView english = subjects.get(1);

        List<BeceResultView> initialResults = beceService.importResults(reg.id(), List.of(
                new BeceSubjectScoreSpec(math.id(), 1),
                new BeceSubjectScoreSpec(english.id(), 2)
        ));

        assertThat(initialResults).hasSize(2);

        // Update math score idempotently (e.g. grade remarking/correction)
        List<BeceResultView> updatedResults = beceService.importResults(reg.id(), List.of(
                new BeceSubjectScoreSpec(math.id(), 2)
        ));

        assertThat(updatedResults).hasSize(1);
        assertThat(updatedResults.get(0).grade()).isEqualTo(2);

        List<BeceResultView> allResults = beceQueryService.listResults(reg.id());
        assertThat(allResults).hasSize(2);
    }

    /**
     * Regression test: BECE_REGISTER/BECE_SCORE_ENTER were referenced by {@code BeceController}
     * but never seeded into any role (V3 is immutable, and no later migration added them until
     * V15) — every BECE endpoint was permanently unreachable (403 for literally every account,
     * including HEAD_OF_SCHOOL) despite the service-layer tests above all passing, because those
     * tests call {@link BeceService} directly and never exercise the permission gate.
     */
    @Test
    void schoolAdminWithBeceRegisterPermissionCanReachTheRealEndpoint() throws Exception {
        StudentView student = createStudent("Efua", "Boadi");
        EnrollmentView enrollment = enrollmentService.enroll(student.id(), jhs3Class.id(), year.id(), null);

        AccountCreationResult account = accountProvisioningService.createAccount(
                PersonType.STAFF, UUID.randomUUID(), "school.admin.bece." + UUID.randomUUID(), "+233201" + (counter++), null);
        accountProvisioningService.assignRole(account.account().id(), "SCHOOL_ADMIN");
        AuthTokens tokens = authenticationService.login(
                account.account().loginIdentifier(), account.temporaryPassword(), "127.0.0.1");

        String body = "{\"enrollmentId\":\"" + enrollment.id() + "\",\"indexNumber\":\"1002003099\"}";

        mockMvc.perform(
                        post("/api/v1/bece/registrations")
                                .header("Authorization", "Bearer " + tokens.accessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isCreated());
    }
}
