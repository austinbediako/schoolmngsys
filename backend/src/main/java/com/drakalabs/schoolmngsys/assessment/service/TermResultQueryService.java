package com.drakalabs.schoolmngsys.assessment.service;

import com.drakalabs.schoolmngsys.assessment.repository.TermResultRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** FR-RES-03/05: current subject results for a term, and the full revision history (BR-AA-006). */
@Service
public class TermResultQueryService {

    private final TermResultRepository termResultRepository;

    public TermResultQueryService(TermResultRepository termResultRepository) {
        this.termResultRepository = termResultRepository;
    }

    @Transactional(readOnly = true)
    public List<TermResultView> currentForEnrollment(UUID enrollmentId, UUID termId) {
        return termResultRepository.findByEnrollmentIdAndTermIdAndArchivedAtIsNull(enrollmentId, termId).stream()
                .map(TermResultView::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TermResultView> currentForSubject(UUID classSubjectOfferingId, UUID termId) {
        return termResultRepository.findByClassSubjectOfferingIdAndTermIdAndArchivedAtIsNull(classSubjectOfferingId, termId)
                .stream()
                .map(TermResultView::from)
                .toList();
    }

    /** Includes archived/superseded rows — BR-AA-006's "never destroy history" for this enrollment's results. */
    @Transactional(readOnly = true)
    public List<TermResultView> fullHistory(UUID enrollmentId) {
        return termResultRepository.findByEnrollmentIdOrderByCreatedAtDesc(enrollmentId).stream().map(TermResultView::from).toList();
    }
}
