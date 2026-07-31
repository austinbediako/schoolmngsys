package com.drakalabs.schoolmngsys.assessment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.drakalabs.schoolmngsys.AbstractIntegrationTest;
import com.drakalabs.schoolmngsys.academics.service.AcademicYearService;
import com.drakalabs.schoolmngsys.academics.service.AcademicYearView;
import com.drakalabs.schoolmngsys.academics.service.TermSpec;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** WP-6 (docs/14 §5): A-04 default grade bands, A-03 default weighting, one-scale-per-year (BR-AA-001/005). */
class GradeScaleServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private GradeScaleService gradeScaleService;

    @Autowired
    private AcademicYearService academicYearService;

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

    @Test
    void creatingTheDefaultScaleSeedsTheConfirmedA03WeightingAndA04Bands() {
        AcademicYearView year = newYear();

        GradeScaleView scale = gradeScaleService.createDefault(year.id());

        assertThat(scale.sbaWeightPercent()).isEqualByComparingTo("30.00");
        assertThat(scale.examWeightPercent()).isEqualByComparingTo("70.00");
        assertThat(scale.bands()).hasSize(6);
        assertThat(scale.bands()).extracting(GradeBandView::grade).containsExactlyInAnyOrder("A", "B", "C", "D", "E", "F");
    }

    @Test
    void resolveGradeCoversEveryBandBoundaryCorrectly() {
        AcademicYearView year = newYear();
        gradeScaleService.createDefault(year.id());

        assertThat(gradeScaleService.resolveGrade(year.id(), new BigDecimal("100.0"))).isEqualTo("A");
        assertThat(gradeScaleService.resolveGrade(year.id(), new BigDecimal("80.0"))).isEqualTo("A");
        assertThat(gradeScaleService.resolveGrade(year.id(), new BigDecimal("79.9"))).isEqualTo("B");
        assertThat(gradeScaleService.resolveGrade(year.id(), new BigDecimal("70.0"))).isEqualTo("B");
        assertThat(gradeScaleService.resolveGrade(year.id(), new BigDecimal("69.9"))).isEqualTo("C");
        assertThat(gradeScaleService.resolveGrade(year.id(), new BigDecimal("60.0"))).isEqualTo("C");
        assertThat(gradeScaleService.resolveGrade(year.id(), new BigDecimal("59.9"))).isEqualTo("D");
        assertThat(gradeScaleService.resolveGrade(year.id(), new BigDecimal("50.0"))).isEqualTo("D");
        assertThat(gradeScaleService.resolveGrade(year.id(), new BigDecimal("49.9"))).isEqualTo("E");
        assertThat(gradeScaleService.resolveGrade(year.id(), new BigDecimal("40.0"))).isEqualTo("E");
        assertThat(gradeScaleService.resolveGrade(year.id(), new BigDecimal("39.9"))).isEqualTo("F");
        assertThat(gradeScaleService.resolveGrade(year.id(), new BigDecimal("0.0"))).isEqualTo("F");
    }

    @Test
    void aSecondGradeScaleForTheSameYearIsRejected() {
        AcademicYearView year = newYear();
        gradeScaleService.createDefault(year.id());

        assertThatThrownBy(() -> gradeScaleService.createDefault(year.id()))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-AA-005"));
    }

    @Test
    void customWeightsThatDoNotSumTo100AreRejected() {
        AcademicYearView year = newYear();

        assertThatThrownBy(
                        () -> gradeScaleService.createCustom(
                                year.id(),
                                new BigDecimal("40"),
                                new BigDecimal("50"),
                                List.of(new GradeScaleService.BandSpec(new BigDecimal("0"), new BigDecimal("100"), "P", "Pass"))))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-AA-001"));
    }
}
