package com.drakalabs.schoolmngsys.academics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.drakalabs.schoolmngsys.AbstractIntegrationTest;
import com.drakalabs.schoolmngsys.academics.domain.AcademicYearStatus;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** WP-2 test plan (docs/14 §5): "three-terms invariant" (BR-AS-001). */
class AcademicYearServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AcademicYearService academicYearService;

    private List<TermSpec> threeTerms(int year) {
        return List.of(
                new TermSpec(1, LocalDate.of(year, 9, 1), LocalDate.of(year, 12, 15), 70),
                new TermSpec(2, LocalDate.of(year + 1, 1, 5), LocalDate.of(year + 1, 4, 4), 60),
                new TermSpec(3, LocalDate.of(year + 1, 4, 25), LocalDate.of(year + 1, 8, 1), 65));
    }

    @Test
    void createYearWithExactlyThreeTermsSucceeds() {
        String label = "Y-" + UUID.randomUUID();

        AcademicYearView year = academicYearService.createYear(label, LocalDate.of(2026, 9, 1), LocalDate.of(2027, 8, 1), threeTerms(2026));

        assertThat(year.label()).isEqualTo(label);
        assertThat(year.status()).isEqualTo(AcademicYearStatus.PLANNED);
    }

    @Test
    void createYearWithFewerThanThreeTermsFails() {
        String label = "Y-" + UUID.randomUUID();
        List<TermSpec> twoTerms = List.of(threeTerms(2026).get(0), threeTerms(2026).get(1));

        assertThatThrownBy(
                        () -> academicYearService.createYear(
                                label, LocalDate.of(2026, 9, 1), LocalDate.of(2027, 8, 1), twoTerms))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-AS-001"));
    }

    @Test
    void createYearWithDuplicateTermNumbersFails() {
        String label = "Y-" + UUID.randomUUID();
        List<TermSpec> terms = threeTerms(2026);
        List<TermSpec> duplicated = List.of(terms.get(0), terms.get(0), terms.get(2));

        assertThatThrownBy(
                        () -> academicYearService.createYear(
                                label, LocalDate.of(2026, 9, 1), LocalDate.of(2027, 8, 1), duplicated))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void onlyOneAcademicYearCanBeActiveAtATime() {
        AcademicYearView first = academicYearService.createYear(
                "Y-" + UUID.randomUUID(), LocalDate.of(2020, 9, 1), LocalDate.of(2021, 8, 1), threeTerms(2020));
        AcademicYearView second = academicYearService.createYear(
                "Y-" + UUID.randomUUID(), LocalDate.of(2021, 9, 1), LocalDate.of(2022, 8, 1), threeTerms(2021));

        academicYearService.activateYear(first.id());

        assertThatThrownBy(() -> academicYearService.activateYear(second.id()))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-AS-001"));

        academicYearService.closeYear(first.id());
        AcademicYearView activatedSecond = academicYearService.activateYear(second.id());
        assertThat(activatedSecond.status()).isEqualTo(AcademicYearStatus.ACTIVE);
    }
}
