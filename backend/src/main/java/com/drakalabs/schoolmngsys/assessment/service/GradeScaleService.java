package com.drakalabs.schoolmngsys.assessment.service;

import com.drakalabs.schoolmngsys.assessment.domain.DefaultGradeBand;
import com.drakalabs.schoolmngsys.assessment.domain.GradeBand;
import com.drakalabs.schoolmngsys.assessment.domain.GradeScale;
import com.drakalabs.schoolmngsys.assessment.repository.DefaultGradeBandRepository;
import com.drakalabs.schoolmngsys.assessment.repository.GradeBandRepository;
import com.drakalabs.schoolmngsys.assessment.repository.GradeScaleRepository;
import com.drakalabs.schoolmngsys.shared.audit.Audited;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** BR-AA-001/005: one grade scale (weighting + bands) per academic year. */
@Service
public class GradeScaleService {

    private static final BigDecimal DEFAULT_SBA_WEIGHT = new BigDecimal("30.00"); // A-03
    private static final BigDecimal DEFAULT_EXAM_WEIGHT = new BigDecimal("70.00"); // A-03

    private final GradeScaleRepository gradeScaleRepository;
    private final GradeBandRepository gradeBandRepository;
    private final DefaultGradeBandRepository defaultGradeBandRepository;

    public GradeScaleService(
            GradeScaleRepository gradeScaleRepository,
            GradeBandRepository gradeBandRepository,
            DefaultGradeBandRepository defaultGradeBandRepository) {
        this.gradeScaleRepository = gradeScaleRepository;
        this.gradeBandRepository = gradeBandRepository;
        this.defaultGradeBandRepository = defaultGradeBandRepository;
    }

    @Audited(action = "GRADE_SCALE_CREATED", entityType = "GradeScale")
    @Transactional
    public GradeScaleView createDefault(UUID academicYearId) {
        return create(academicYearId, DEFAULT_SBA_WEIGHT, DEFAULT_EXAM_WEIGHT, defaultBands());
    }

    @Audited(action = "GRADE_SCALE_CREATED", entityType = "GradeScale")
    @Transactional
    public GradeScaleView createCustom(
            UUID academicYearId, BigDecimal sbaWeightPercent, BigDecimal examWeightPercent, List<BandSpec> bands) {
        if (sbaWeightPercent.add(examWeightPercent).compareTo(new BigDecimal("100")) != 0) {
            throw new BusinessRuleViolationException("BR-AA-001", "SBA and exam weights must sum to 100");
        }
        return create(academicYearId, sbaWeightPercent, examWeightPercent, bands);
    }

    private GradeScaleView create(UUID academicYearId, BigDecimal sbaWeight, BigDecimal examWeight, List<BandSpec> bands) {
        gradeScaleRepository
                .findByAcademicYearIdAndArchivedAtIsNull(academicYearId)
                .ifPresent(
                        existing -> {
                            throw new BusinessRuleViolationException(
                                    "BR-AA-005", "This academic year already has a grade scale");
                        });

        GradeScale scale = gradeScaleRepository.save(new GradeScale(academicYearId, sbaWeight, examWeight));
        List<GradeBandView> bandViews =
                bands.stream()
                        .map(
                                spec -> GradeBandView.from(
                                        gradeBandRepository.save(
                                                new GradeBand(scale, spec.minScore(), spec.maxScore(), spec.grade(), spec.description()))))
                        .toList();

        return GradeScaleView.from(scale, bandViews);
    }

    @Transactional(readOnly = true)
    public GradeScaleView getByYear(UUID academicYearId) {
        GradeScale scale = gradeScaleRepository
                .findByAcademicYearIdAndArchivedAtIsNull(academicYearId)
                .orElseThrow(() -> new NotFoundException("No grade scale configured for academic year " + academicYearId));
        List<GradeBandView> bands =
                gradeBandRepository.findByGradeScaleIdAndArchivedAtIsNull(scale.getId()).stream().map(GradeBandView::from).toList();
        return GradeScaleView.from(scale, bands);
    }

    /** BR-AA-005: resolves a weighted total to its letter grade under the year's active scale. */
    @Transactional(readOnly = true)
    public String resolveGrade(UUID academicYearId, BigDecimal weightedTotal) {
        GradeScaleView scale = getByYear(academicYearId);
        return scale.bands().stream()
                .filter(band -> weightedTotal.compareTo(band.minScore()) >= 0 && weightedTotal.compareTo(band.maxScore()) <= 0)
                .map(GradeBandView::grade)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No grade band covers score " + weightedTotal));
    }

    private List<BandSpec> defaultBands() {
        return defaultGradeBandRepository.findAll().stream().map(BandSpec::from).toList();
    }

    public record BandSpec(BigDecimal minScore, BigDecimal maxScore, String grade, String description) {
        public static BandSpec from(DefaultGradeBand band) {
            return new BandSpec(band.getMinScore(), band.getMaxScore(), band.getGrade(), band.getDescription());
        }
    }
}
