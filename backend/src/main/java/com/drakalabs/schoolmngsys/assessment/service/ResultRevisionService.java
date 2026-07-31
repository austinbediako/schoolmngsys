package com.drakalabs.schoolmngsys.assessment.service;

import com.drakalabs.schoolmngsys.academics.service.ClassQueryService;
import com.drakalabs.schoolmngsys.academics.service.ClassSubjectOfferingView;
import com.drakalabs.schoolmngsys.assessment.domain.ResultStatus;
import com.drakalabs.schoolmngsys.assessment.domain.TermResult;
import com.drakalabs.schoolmngsys.assessment.domain.TermResultsPublished;
import com.drakalabs.schoolmngsys.assessment.repository.TermResultRepository;
import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentQueryService;
import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentView;
import com.drakalabs.schoolmngsys.shared.audit.Audited;
import com.drakalabs.schoolmngsys.shared.event.DomainEventPublisher;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * BR-AA-006: a PUBLISHED result is immutable; a correction creates a new versioned row (reason +
 * the acting approver, captured via audit) and archives the old one — never edits it in place.
 * The report card regenerates (the whole class re-ranks, since one student's total changed) and
 * guardians are re-notified (same {@link TermResultsPublished} event WP-8 will subscribe to).
 */
@Service
public class ResultRevisionService {

    private final TermResultRepository termResultRepository;
    private final ResultComputationService resultComputationService;
    private final ClassQueryService classQueryService;
    private final EnrollmentQueryService enrollmentQueryService;
    private final ReportCardService reportCardService;
    private final DomainEventPublisher domainEventPublisher;

    public ResultRevisionService(
            TermResultRepository termResultRepository,
            ResultComputationService resultComputationService,
            ClassQueryService classQueryService,
            EnrollmentQueryService enrollmentQueryService,
            ReportCardService reportCardService,
            DomainEventPublisher domainEventPublisher) {
        this.termResultRepository = termResultRepository;
        this.resultComputationService = resultComputationService;
        this.classQueryService = classQueryService;
        this.enrollmentQueryService = enrollmentQueryService;
        this.reportCardService = reportCardService;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Audited(action = "RESULT_REVISED", entityType = "TermResult")
    @Transactional
    public TermResultView revise(UUID existingResultId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessRuleViolationException("BR-AA-006", "A revision requires a reason");
        }

        TermResult old = termResultRepository
                .findById(existingResultId)
                .orElseThrow(() -> new NotFoundException("No such term result: " + existingResultId));
        if (old.getStatus() != ResultStatus.PUBLISHED) {
            throw new BusinessRuleViolationException("BR-AA-006", "Only a PUBLISHED result can be revised");
        }

        TermResultView recomputed =
                resultComputationService.recomputeOne(old.getEnrollmentId(), old.getClassSubjectOfferingId(), old.getTermId());

        // Hibernate flushes all pending INSERTs before any UPDATEs in a single flush, regardless
        // of call order — so archiving `old` must hit the DB *before* the new row is inserted, or
        // both rows briefly coexist as "current" and violate the partial unique index. Flushing
        // here (not just saving) forces that ordering, still inside this one transaction.
        old.archive();
        termResultRepository.saveAndFlush(old);

        TermResult revised = new TermResult(
                old.getEnrollmentId(),
                old.getClassSubjectOfferingId(),
                old.getTermId(),
                recomputed.sbaTotal(),
                recomputed.examTotal(),
                recomputed.weightedTotal(),
                recomputed.grade());
        revised.publish();
        revised.setRevisionInfo(old.getResultVersion() + 1, reason);
        termResultRepository.save(revised);

        old.supersede(revised.getId());
        termResultRepository.save(old);

        ClassSubjectOfferingView offering = classQueryService.getOffering(old.getClassSubjectOfferingId());
        List<UUID> enrollmentIds = enrollmentQueryService.roster(offering.classId(), offering.academicYearId()).stream()
                .map(EnrollmentView::id)
                .toList();
        reportCardService.publishForClass(enrollmentIds, old.getTermId());

        domainEventPublisher.publish(
                new TermResultsPublished(offering.classId(), offering.academicYearId(), old.getTermId(), enrollmentIds));

        return TermResultView.from(revised);
    }
}
