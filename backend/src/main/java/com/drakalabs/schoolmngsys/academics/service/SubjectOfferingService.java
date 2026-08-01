package com.drakalabs.schoolmngsys.academics.service;

import com.drakalabs.schoolmngsys.academics.domain.AcademicYear;
import com.drakalabs.schoolmngsys.academics.domain.ClassSubjectOffering;
import com.drakalabs.schoolmngsys.academics.domain.SchoolClass;
import com.drakalabs.schoolmngsys.academics.domain.Subject;
import com.drakalabs.schoolmngsys.academics.repository.AcademicYearRepository;
import com.drakalabs.schoolmngsys.academics.repository.ClassRepository;
import com.drakalabs.schoolmngsys.academics.repository.ClassSubjectOfferingRepository;
import com.drakalabs.schoolmngsys.academics.repository.SubjectRepository;
import com.drakalabs.schoolmngsys.shared.audit.Audited;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Per-year subject offerings per class, with teacher assignment (BR-AS-006, FR-ACAD-04). */
@Service
public class SubjectOfferingService {

    private final ClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final AcademicYearRepository academicYearRepository;
    private final ClassSubjectOfferingRepository classSubjectOfferingRepository;

    public SubjectOfferingService(
            ClassRepository classRepository,
            SubjectRepository subjectRepository,
            AcademicYearRepository academicYearRepository,
            ClassSubjectOfferingRepository classSubjectOfferingRepository) {
        this.classRepository = classRepository;
        this.subjectRepository = subjectRepository;
        this.academicYearRepository = academicYearRepository;
        this.classSubjectOfferingRepository = classSubjectOfferingRepository;
    }

    @Transactional(readOnly = true)
    public List<ClassSubjectOfferingView> listOfferings(UUID classId, UUID academicYearId) {
        return classSubjectOfferingRepository
                .findBySchoolClassIdAndAcademicYearIdAndArchivedAtIsNull(classId, academicYearId)
                .stream()
                .map(ClassSubjectOfferingView::from)
                .toList();
    }

    @Audited(action = "SUBJECT_OFFERING_CREATED", entityType = "ClassSubjectOffering")
    @Transactional
    public ClassSubjectOfferingView createOffering(UUID classId, UUID subjectId, UUID academicYearId) {
        SchoolClass schoolClass =
                classRepository.findById(classId).orElseThrow(() -> new NotFoundException("No such class: " + classId));
        Subject subject = subjectRepository
                .findById(subjectId)
                .orElseThrow(() -> new NotFoundException("No such subject: " + subjectId));
        AcademicYear academicYear = academicYearRepository
                .findById(academicYearId)
                .orElseThrow(() -> new NotFoundException("No such academic year: " + academicYearId));

        if (!subject.appliesTo(schoolClass.getClassLevel())) {
            throw new BusinessRuleViolationException(
                    "BR-AS-006", subject.getName() + " is not offered at " + schoolClass.getClassLevel().getCanonicalName());
        }

        classSubjectOfferingRepository
                .findBySchoolClassIdAndSubjectIdAndAcademicYearIdAndArchivedAtIsNull(classId, subjectId, academicYearId)
                .ifPresent(
                        existing -> {
                            throw new BusinessRuleViolationException(
                                    "BR-AS-006", "This subject is already offered to this class for this year");
                        });

        return ClassSubjectOfferingView.from(classSubjectOfferingRepository.save(new ClassSubjectOffering(schoolClass, subject, academicYear)));
    }

    @Audited(action = "SUBJECT_TEACHER_ASSIGNED", entityType = "ClassSubjectOffering")
    @Transactional
    public ClassSubjectOfferingView assignTeacher(UUID offeringId, UUID teacherStaffId) {
        ClassSubjectOffering offering = classSubjectOfferingRepository
                .findById(offeringId)
                .orElseThrow(() -> new NotFoundException("No such subject offering: " + offeringId));
        offering.assignTeacher(teacherStaffId);
        return ClassSubjectOfferingView.from(classSubjectOfferingRepository.save(offering));
    }
}
