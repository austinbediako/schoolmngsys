package com.drakalabs.schoolmngsys.academics.service;

import com.drakalabs.schoolmngsys.academics.domain.AcademicYear;
import com.drakalabs.schoolmngsys.academics.domain.AcademicYearStatus;
import com.drakalabs.schoolmngsys.academics.domain.Term;
import com.drakalabs.schoolmngsys.academics.repository.AcademicYearRepository;
import com.drakalabs.schoolmngsys.academics.repository.TermRepository;
import com.drakalabs.schoolmngsys.shared.audit.Audited;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Academic year/term lifecycle (BR-AS-001/007, FR-ACAD-01). */
@Service
public class AcademicYearService {

    private final AcademicYearRepository academicYearRepository;
    private final TermRepository termRepository;

    public AcademicYearService(AcademicYearRepository academicYearRepository, TermRepository termRepository) {
        this.academicYearRepository = academicYearRepository;
        this.termRepository = termRepository;
    }

    @Audited(action = "ACADEMIC_YEAR_CREATED", entityType = "AcademicYear")
    @Transactional
    public AcademicYearView createYear(String label, LocalDate startDate, LocalDate endDate, List<TermSpec> termSpecs) {
        Set<Integer> termNumbers = termSpecs.stream().map(TermSpec::termNumber).collect(java.util.stream.Collectors.toSet());
        if (termSpecs.size() != 3 || !termNumbers.equals(Set.of(1, 2, 3))) {
            throw new BusinessRuleViolationException(
                    "BR-AS-001", "An academic year must have exactly three terms, numbered 1, 2, and 3");
        }

        AcademicYear year = new AcademicYear(label, startDate, endDate);
        academicYearRepository.save(year);

        for (TermSpec spec : termSpecs) {
            termRepository.save(
                    new Term(year, spec.termNumber(), spec.startDate(), spec.endDate(), spec.expectedSchoolDays()));
        }

        return AcademicYearView.from(year);
    }

    @Audited(action = "ACADEMIC_YEAR_ACTIVATED", entityType = "AcademicYear")
    @Transactional
    public AcademicYearView activateYear(UUID yearId) {
        AcademicYear year = getYear(yearId);

        academicYearRepository
                .findByStatusAndArchivedAtIsNull(AcademicYearStatus.ACTIVE)
                .filter(active -> !active.getId().equals(yearId))
                .ifPresent(
                        active -> {
                            throw new BusinessRuleViolationException(
                                    "BR-AS-001",
                                    "Academic year " + active.getLabel() + " is already active; close it first");
                        });

        year.activate();
        return AcademicYearView.from(academicYearRepository.save(year));
    }

    /**
     * BR-AS-007 closure precondition checklist (results published/voided, promotions finalized) is
     * enforced here only up to what WP-2 alone can check; the assessment/progression preconditions
     * become enforceable once WP-6/WP-9 exist.
     */
    @Audited(action = "ACADEMIC_YEAR_CLOSED", entityType = "AcademicYear")
    @Transactional
    public AcademicYearView closeYear(UUID yearId) {
        AcademicYear year = getYear(yearId);
        if (year.getStatus() != AcademicYearStatus.ACTIVE) {
            throw new BusinessRuleViolationException("BR-AS-007", "Only the active academic year can be closed");
        }
        year.close();
        return AcademicYearView.from(academicYearRepository.save(year));
    }

    private AcademicYear getYear(UUID yearId) {
        return academicYearRepository.findById(yearId).orElseThrow(() -> new NotFoundException("No such academic year: " + yearId));
    }
}
