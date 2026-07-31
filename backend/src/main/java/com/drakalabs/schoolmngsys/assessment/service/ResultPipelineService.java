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
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** BR-AA-003: DRAFT/SUBMITTED -> HOD_APPROVED -> PUBLISHED role gates (FR-RES-04). */
@Service
public class ResultPipelineService {

    private final TermResultRepository termResultRepository;
    private final ClassQueryService classQueryService;
    private final EnrollmentQueryService enrollmentQueryService;
    private final ReportCardService reportCardService;
    private final DomainEventPublisher domainEventPublisher;

    public ResultPipelineService(
            TermResultRepository termResultRepository,
            ClassQueryService classQueryService,
            EnrollmentQueryService enrollmentQueryService,
            ReportCardService reportCardService,
            DomainEventPublisher domainEventPublisher) {
        this.termResultRepository = termResultRepository;
        this.classQueryService = classQueryService;
        this.enrollmentQueryService = enrollmentQueryService;
        this.reportCardService = reportCardService;
        this.domainEventPublisher = domainEventPublisher;
    }

    /** HoD reviews a subject's results for a term (WF-04: "HoD reviews dept"). */
    @Audited(action = "RESULTS_APPROVED", entityType = "TermResult")
    @Transactional
    public List<TermResultView> approveSubjectResults(UUID classSubjectOfferingId, UUID termId) {
        List<TermResult> results = termResultRepository.findByClassSubjectOfferingIdAndTermIdAndArchivedAtIsNull(
                classSubjectOfferingId, termId);
        if (results.isEmpty()) {
            throw new BusinessRuleViolationException("BR-AA-003", "No submitted results found for this subject/term");
        }
        for (TermResult result : results) {
            if (result.getStatus() != ResultStatus.SUBMITTED) {
                throw new BusinessRuleViolationException(
                        "BR-AA-003", "Only SUBMITTED results can be approved (found " + result.getStatus() + ")");
            }
        }
        results.forEach(TermResult::approve);
        results.forEach(termResultRepository::save);
        return results.stream().map(TermResultView::from).toList();
    }

    /** Head publishes a whole class's results for a term (WF-04: "Head PUBLISHes class"). */
    @Audited(action = "RESULTS_PUBLISHED", entityType = "TermResult")
    @Transactional
    public List<TermResultView> publishClassResults(UUID classId, UUID academicYearId, UUID termId) {
        List<ClassSubjectOfferingView> offerings = classQueryService.listOfferings(classId, academicYearId);
        if (offerings.isEmpty()) {
            throw new BusinessRuleViolationException("BR-AA-003", "This class has no subject offerings for this year");
        }

        List<TermResult> allResults = offerings.stream()
                .flatMap(
                        offering -> termResultRepository
                                .findByClassSubjectOfferingIdAndTermIdAndArchivedAtIsNull(offering.id(), termId)
                                .stream())
                .toList();

        if (allResults.isEmpty()) {
            throw new BusinessRuleViolationException("BR-AA-003", "No approved results found for this class/term");
        }
        for (TermResult result : allResults) {
            if (result.getStatus() != ResultStatus.HOD_APPROVED) {
                throw new BusinessRuleViolationException(
                        "BR-AA-003", "Every subject result must be HOD_APPROVED before the class can be published");
            }
        }

        allResults.forEach(TermResult::publish);
        allResults.forEach(termResultRepository::save);

        List<UUID> enrollmentIds = enrollmentQueryService.roster(classId, academicYearId).stream()
                .map(EnrollmentView::id)
                .toList();
        reportCardService.publishForClass(enrollmentIds, termId);

        domainEventPublisher.publish(new TermResultsPublished(classId, academicYearId, termId, enrollmentIds));

        return allResults.stream().map(TermResultView::from).toList();
    }
}
