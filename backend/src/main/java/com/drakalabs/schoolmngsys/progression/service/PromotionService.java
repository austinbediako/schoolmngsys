package com.drakalabs.schoolmngsys.progression.service;

import com.drakalabs.schoolmngsys.academics.service.AcademicYearQueryService;
import com.drakalabs.schoolmngsys.academics.service.ClassLevelView;
import com.drakalabs.schoolmngsys.academics.service.ClassQueryService;
import com.drakalabs.schoolmngsys.academics.service.ClassView;
import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentQueryService;
import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentService;
import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentView;
import com.drakalabs.schoolmngsys.progression.domain.PromotionDecision;
import com.drakalabs.schoolmngsys.progression.domain.PromotionDecisionStatus;
import com.drakalabs.schoolmngsys.progression.domain.PromotionDecisionType;
import com.drakalabs.schoolmngsys.progression.domain.PromotionRun;
import com.drakalabs.schoolmngsys.progression.domain.PromotionRunStatus;
import com.drakalabs.schoolmngsys.progression.domain.StudentGraduated;
import com.drakalabs.schoolmngsys.progression.domain.StudentPromoted;
import com.drakalabs.schoolmngsys.progression.domain.StudentRepeated;
import com.drakalabs.schoolmngsys.progression.repository.PromotionDecisionRepository;
import com.drakalabs.schoolmngsys.progression.repository.PromotionRunRepository;
import com.drakalabs.schoolmngsys.shared.event.DomainEventPublisher;
import com.drakalabs.schoolmngsys.shared.security.CurrentActorProvider;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromotionService {

    private final PromotionRunRepository promotionRunRepository;
    private final PromotionDecisionRepository promotionDecisionRepository;
    private final EnrollmentQueryService enrollmentQueryService;
    private final EnrollmentService enrollmentService;
    private final AcademicYearQueryService academicYearQueryService;
    private final ClassQueryService classQueryService;
    private final DomainEventPublisher eventPublisher;
    private final CurrentActorProvider currentActorProvider;

    public PromotionService(
            PromotionRunRepository promotionRunRepository,
            PromotionDecisionRepository promotionDecisionRepository,
            EnrollmentQueryService enrollmentQueryService,
            EnrollmentService enrollmentService,
            AcademicYearQueryService academicYearQueryService,
            ClassQueryService classQueryService,
            DomainEventPublisher eventPublisher,
            CurrentActorProvider currentActorProvider) {
        this.promotionRunRepository = promotionRunRepository;
        this.promotionDecisionRepository = promotionDecisionRepository;
        this.enrollmentQueryService = enrollmentQueryService;
        this.enrollmentService = enrollmentService;
        this.academicYearQueryService = academicYearQueryService;
        this.classQueryService = classQueryService;
        this.eventPublisher = eventPublisher;
        this.currentActorProvider = currentActorProvider;
    }

    @Transactional
    public PromotionRunView initiateRun(UUID sourceAcademicYearId, UUID targetAcademicYearId) {
        academicYearQueryService.get(sourceAcademicYearId);
        academicYearQueryService.get(targetAcademicYearId);

        if (promotionRunRepository.findBySourceAcademicYearIdAndArchivedAtIsNull(sourceAcademicYearId).isPresent()) {
            throw new BusinessRuleViolationException("BR-PR-001", "Promotion run already initiated for source academic year: " + sourceAcademicYearId);
        }

        PromotionRun run = new PromotionRun(sourceAcademicYearId, targetAcademicYearId);
        run = promotionRunRepository.save(run);

        List<EnrollmentView> activeEnrollments = enrollmentQueryService.listByYear(sourceAcademicYearId);

        for (EnrollmentView enrollment : activeEnrollments) {
            ClassView sourceClass = classQueryService.get(enrollment.classId());
            ClassLevelView sourceLevel = classQueryService.getClassLevel(sourceClass.classLevelId());

            PromotionDecisionType decisionType;
            UUID targetLevelId = null;

            if (sourceLevel.sequence() >= 13) {
                // JHS 3 / Basic 9 -> GRADUATE (BR-PR-004)
                decisionType = PromotionDecisionType.GRADUATE;
            } else {
                // Default PROMOTE (BR-PR-002 / A-08) -> next rung in sequence (BR-PR-003)
                decisionType = PromotionDecisionType.PROMOTE;
                Optional<ClassLevelView> nextLevelOpt = classQueryService.getClassLevelBySequence(sourceLevel.sequence() + 1);
                if (nextLevelOpt.isPresent()) {
                    targetLevelId = nextLevelOpt.get().id();
                }
            }

            PromotionDecision decision = new PromotionDecision(
                    run.getId(),
                    enrollment.studentId(),
                    sourceClass.id(),
                    sourceLevel.id(),
                    decisionType,
                    targetLevelId,
                    null
            );
            promotionDecisionRepository.save(decision);
        }

        run.propose();
        return PromotionRunView.from(run);
    }

    @Transactional
    public PromotionDecisionView proposeException(
            UUID decisionId,
            PromotionDecisionType decisionType,
            UUID targetClassLevelId,
            String justification) {
        PromotionDecision decision = promotionDecisionRepository.findById(decisionId)
                .orElseThrow(() -> new NotFoundException("No such promotion decision: " + decisionId));

        PromotionRun run = promotionRunRepository.findById(decision.getPromotionRunId())
                .orElseThrow(() -> new NotFoundException("No such promotion run: " + decision.getPromotionRunId()));

        if (run.getStatus() == PromotionRunStatus.EXECUTED) {
            throw new BusinessRuleViolationException("BR-PR-001", "Cannot modify decisions for an already executed promotion run");
        }

        // BR-PR-002 / BR-PR-003: REPEAT or level skip requires written justification
        if (decisionType == PromotionDecisionType.REPEAT || justificationNeeded(decision, decisionType, targetClassLevelId)) {
            if (justification == null || justification.isBlank()) {
                throw new BusinessRuleViolationException("BR-PR-002", "Written justification is required for repeat or non-standard promotion decisions");
            }
        }

        UUID finalTargetLevelId = targetClassLevelId;
        if (decisionType == PromotionDecisionType.REPEAT) {
            finalTargetLevelId = decision.getSourceClassLevelId();
        } else if (decisionType == PromotionDecisionType.GRADUATE) {
            finalTargetLevelId = null;
        }

        decision.updateDecision(decisionType, finalTargetLevelId, justification);
        return PromotionDecisionView.from(decision);
    }

    @Transactional
    public PromotionDecisionView assignTargetClass(UUID decisionId, UUID targetClassId) {
        PromotionDecision decision = promotionDecisionRepository.findById(decisionId)
                .orElseThrow(() -> new NotFoundException("No such promotion decision: " + decisionId));

        if (targetClassId != null) {
            classQueryService.get(targetClassId);
        }

        decision.assignTargetClass(targetClassId);
        return PromotionDecisionView.from(decision);
    }

    @Transactional
    public PromotionRunView approveRun(UUID runId) {
        PromotionRun run = promotionRunRepository.findById(runId)
                .orElseThrow(() -> new NotFoundException("No such promotion run: " + runId));

        if (run.getStatus() == PromotionRunStatus.EXECUTED) {
            throw new BusinessRuleViolationException("BR-PR-001", "Promotion run is already executed");
        }

        UUID actorId = currentActorProvider.currentActorId().orElse(null);
        run.approve();

        List<PromotionDecision> decisions = promotionDecisionRepository.findByPromotionRunIdAndArchivedAtIsNull(runId);
        for (PromotionDecision decision : decisions) {
            decision.approve(actorId);
        }

        return PromotionRunView.from(run);
    }

    @Transactional
    public PromotionRunView executeRun(UUID runId) {
        PromotionRun run = promotionRunRepository.findById(runId)
                .orElseThrow(() -> new NotFoundException("No such promotion run: " + runId));

        if (run.getStatus() == PromotionRunStatus.EXECUTED) {
            throw new BusinessRuleViolationException("BR-PR-001", "Promotion run is already executed");
        }

        if (run.getStatus() != PromotionRunStatus.APPROVED) {
            throw new BusinessRuleViolationException("BR-PR-002", "Promotion run must be APPROVED before execution");
        }

        UUID actorId = currentActorProvider.currentActorId().orElse(null);
        List<PromotionDecision> decisions = promotionDecisionRepository.findByPromotionRunIdAndArchivedAtIsNull(runId);

        for (PromotionDecision decision : decisions) {
            if (decision.getDecision() == PromotionDecisionType.GRADUATE) {
                eventPublisher.publish(new StudentGraduated(decision.getStudentId(), run.getSourceAcademicYearId()));
            } else {
                UUID targetClassId = decision.getTargetClassId();
                if (targetClassId == null) {
                    targetClassId = autoResolveTargetClass(decision.getTargetClassLevelId(), decision.getSourceClassId(), run.getTargetAcademicYearId());
                }

                enrollmentService.enroll(decision.getStudentId(), targetClassId, run.getTargetAcademicYearId(), null);

                if (decision.getDecision() == PromotionDecisionType.PROMOTE) {
                    eventPublisher.publish(new StudentPromoted(decision.getStudentId(), decision.getSourceClassId(), targetClassId, run.getTargetAcademicYearId()));
                } else if (decision.getDecision() == PromotionDecisionType.REPEAT) {
                    eventPublisher.publish(new StudentRepeated(decision.getStudentId(), decision.getSourceClassId(), run.getTargetAcademicYearId(), decision.getJustification()));
                }
            }
        }

        run.execute(actorId);
        return PromotionRunView.from(run);
    }

    private boolean justificationNeeded(PromotionDecision decision, PromotionDecisionType newType, UUID targetLevelId) {
        if (newType == PromotionDecisionType.PROMOTE && targetLevelId != null) {
            ClassLevelView sourceLevel = classQueryService.getClassLevel(decision.getSourceClassLevelId());
            ClassLevelView targetLevel = classQueryService.getClassLevel(targetLevelId);
            // Non-standard level skip (BR-PR-003): target level sequence != source level sequence + 1
            return targetLevel.sequence() != sourceLevel.sequence() + 1;
        }
        return false;
    }

    private UUID autoResolveTargetClass(UUID targetLevelId, UUID sourceClassId, UUID targetYearId) {
        List<ClassView> targetLevelClasses = classQueryService.listByLevel(targetLevelId);
        if (targetLevelClasses.isEmpty()) {
            throw new BusinessRuleViolationException("BR-PR-005", "No classes defined for target class level: " + targetLevelId);
        }

        ClassView sourceClass = classQueryService.get(sourceClassId);
        // Try to match stream (e.g. 3A -> 4A)
        String streamName = sourceClass.stream();
        for (ClassView candidate : targetLevelClasses) {
            if (candidate.stream().equals(streamName)) {
                return candidate.id();
            }
        }
        // Fall back to first available class in target level
        return targetLevelClasses.get(0).id();
    }
}
