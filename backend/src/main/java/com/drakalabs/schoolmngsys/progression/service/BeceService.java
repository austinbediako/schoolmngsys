package com.drakalabs.schoolmngsys.progression.service;

import com.drakalabs.schoolmngsys.academics.service.ClassLevelView;
import com.drakalabs.schoolmngsys.academics.service.ClassQueryService;
import com.drakalabs.schoolmngsys.academics.service.ClassView;
import com.drakalabs.schoolmngsys.enrollment.domain.EnrollmentStatus;
import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentQueryService;
import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentView;
import com.drakalabs.schoolmngsys.people.service.StudentQueryService;
import com.drakalabs.schoolmngsys.people.service.StudentView;
import com.drakalabs.schoolmngsys.progression.domain.BeceRegistration;
import com.drakalabs.schoolmngsys.progression.domain.BeceResult;
import com.drakalabs.schoolmngsys.progression.repository.BeceRegistrationRepository;
import com.drakalabs.schoolmngsys.progression.repository.BeceResultRepository;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BeceService {

    private final BeceRegistrationRepository beceRegistrationRepository;
    private final BeceResultRepository beceResultRepository;
    private final EnrollmentQueryService enrollmentQueryService;
    private final StudentQueryService studentQueryService;
    private final ClassQueryService classQueryService;

    public BeceService(
            BeceRegistrationRepository beceRegistrationRepository,
            BeceResultRepository beceResultRepository,
            EnrollmentQueryService enrollmentQueryService,
            StudentQueryService studentQueryService,
            ClassQueryService classQueryService) {
        this.beceRegistrationRepository = beceRegistrationRepository;
        this.beceResultRepository = beceResultRepository;
        this.enrollmentQueryService = enrollmentQueryService;
        this.studentQueryService = studentQueryService;
        this.classQueryService = classQueryService;
    }

    @Transactional
    public BeceRegistrationView registerCandidate(UUID enrollmentId, String indexNumber) {
        if (indexNumber == null || indexNumber.isBlank()) {
            throw new BusinessRuleViolationException("BR-BE-001", "BECE index number is required");
        }

        if (beceRegistrationRepository.findByIndexNumberAndArchivedAtIsNull(indexNumber).isPresent()) {
            throw new BusinessRuleViolationException("BR-BE-001", "BECE index number already registered: " + indexNumber);
        }

        if (beceRegistrationRepository.findByEnrollmentIdAndArchivedAtIsNull(enrollmentId).isPresent()) {
            throw new BusinessRuleViolationException("BR-BE-001", "Student enrollment already registered for BECE: " + enrollmentId);
        }

        EnrollmentView enrollment = enrollmentQueryService.get(enrollmentId);
        if (enrollment.status() != EnrollmentStatus.ACTIVE) {
            throw new BusinessRuleViolationException("BR-BE-001", "Only active enrollments can be registered as BECE candidates");
        }

        ClassView schoolClass = classQueryService.get(enrollment.classId());
        ClassLevelView level = classQueryService.getClassLevel(schoolClass.classLevelId());
        if (level.sequence() < 13) {
            throw new BusinessRuleViolationException("BR-BE-001", "Only JHS 3 / Basic 9 students (sequence 13) can be registered as BECE candidates");
        }

        // Snapshot student bio-data (BR-BE-002)
        StudentView student = studentQueryService.get(enrollment.studentId());
        BeceRegistration registration = new BeceRegistration(
                enrollment.id(),
                student.id(),
                indexNumber,
                student.firstName(),
                student.lastName(),
                student.dateOfBirth()
        );

        registration = beceRegistrationRepository.save(registration);
        return BeceRegistrationView.from(registration);
    }

    @Transactional
    public List<BeceResultView> importResults(UUID registrationId, List<BeceSubjectScoreSpec> scores) {
        BeceRegistration registration = beceRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new NotFoundException("No such BECE registration: " + registrationId));

        List<BeceResultView> results = new ArrayList<>();
        for (BeceSubjectScoreSpec score : scores) {
            // BR-BE-003: Stanine 1-9 check
            if (score.grade() < 1 || score.grade() > 9) {
                throw new BusinessRuleViolationException("BR-BE-003", "BECE grade must be a WAEC stanine between 1 and 9 (received: " + score.grade() + ")");
            }

            Optional<BeceResult> existingOpt = beceResultRepository
                    .findByBeceRegistrationIdAndSubjectIdAndArchivedAtIsNull(registration.getId(), score.subjectId());

            BeceResult result;
            if (existingOpt.isPresent()) {
                result = existingOpt.get();
                result.updateGrade(score.grade());
            } else {
                result = new BeceResult(registration.getId(), score.subjectId(), score.grade());
            }
            result = beceResultRepository.save(result);
            results.add(BeceResultView.from(result));
        }

        return results;
    }
}
