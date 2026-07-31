package com.drakalabs.schoolmngsys.academics.service;

import com.drakalabs.schoolmngsys.academics.domain.AcademicYear;
import com.drakalabs.schoolmngsys.academics.domain.AcademicYearStatus;
import com.drakalabs.schoolmngsys.academics.domain.ClassLevel;
import com.drakalabs.schoolmngsys.academics.domain.ClassSubjectOffering;
import com.drakalabs.schoolmngsys.academics.domain.ClassTeacherAssignment;
import com.drakalabs.schoolmngsys.academics.domain.SchoolClass;
import com.drakalabs.schoolmngsys.academics.domain.Subject;
import com.drakalabs.schoolmngsys.academics.repository.AcademicYearRepository;
import com.drakalabs.schoolmngsys.academics.repository.ClassLevelRepository;
import com.drakalabs.schoolmngsys.academics.repository.ClassRepository;
import com.drakalabs.schoolmngsys.academics.repository.ClassSubjectOfferingRepository;
import com.drakalabs.schoolmngsys.academics.repository.ClassTeacherAssignmentRepository;
import com.drakalabs.schoolmngsys.academics.repository.SubjectRepository;
import com.drakalabs.schoolmngsys.shared.audit.Audited;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Classes, per-year class-teacher assignment, and automatic NaCCA subject defaults (BR-AS-004/005, A-01, FR-ACAD-03). */
@Service
public class ClassService {

    private final ClassRepository classRepository;
    private final ClassLevelRepository classLevelRepository;
    private final AcademicYearRepository academicYearRepository;
    private final ClassTeacherAssignmentRepository classTeacherAssignmentRepository;
    private final SubjectRepository subjectRepository;
    private final ClassSubjectOfferingRepository classSubjectOfferingRepository;

    public ClassService(
            ClassRepository classRepository,
            ClassLevelRepository classLevelRepository,
            AcademicYearRepository academicYearRepository,
            ClassTeacherAssignmentRepository classTeacherAssignmentRepository,
            SubjectRepository subjectRepository,
            ClassSubjectOfferingRepository classSubjectOfferingRepository) {
        this.classRepository = classRepository;
        this.classLevelRepository = classLevelRepository;
        this.academicYearRepository = academicYearRepository;
        this.classTeacherAssignmentRepository = classTeacherAssignmentRepository;
        this.subjectRepository = subjectRepository;
        this.classSubjectOfferingRepository = classSubjectOfferingRepository;
    }

    @Audited(action = "CLASS_CREATED", entityType = "SchoolClass")
    @Transactional
    public ClassView createClass(String classLevelCode, String stream, int capacity) {
        ClassLevel level = classLevelRepository
                .findByCodeAndArchivedAtIsNull(classLevelCode)
                .orElseThrow(() -> new NotFoundException("No such class level: " + classLevelCode));

        classRepository
                .findByClassLevelIdAndStreamAndArchivedAtIsNull(level.getId(), stream)
                .ifPresent(
                        existing -> {
                            throw new BusinessRuleViolationException(
                                    "BR-AS-004", level.getCanonicalName() + " " + stream + " already exists");
                        });

        SchoolClass savedClass = classRepository.save(new SchoolClass(level, stream, capacity));

        // Auto-assign default NaCCA curriculum subjects if an active academic year exists
        academicYearRepository.findByStatusAndArchivedAtIsNull(AcademicYearStatus.ACTIVE).ifPresent(activeYear -> {
            List<Subject> applicableSubjects = subjectRepository.findByArchivedAtIsNull().stream()
                    .filter(s -> s.appliesTo(level))
                    .toList();
            for (Subject subject : applicableSubjects) {
                if (classSubjectOfferingRepository.findBySchoolClassIdAndSubjectIdAndAcademicYearIdAndArchivedAtIsNull(
                        savedClass.getId(), subject.getId(), activeYear.getId()).isEmpty()) {
                    classSubjectOfferingRepository.save(new ClassSubjectOffering(savedClass, subject, activeYear));
                }
            }
        });

        return ClassView.from(savedClass);
    }

    /** BR-AS-005/A-01: one class teacher per class per year, one class per teacher per year. */
    @Audited(action = "CLASS_TEACHER_ASSIGNED", entityType = "ClassTeacherAssignment")
    @Transactional
    public ClassTeacherAssignmentView assignClassTeacher(UUID classId, UUID academicYearId, UUID teacherStaffId) {
        SchoolClass schoolClass =
                classRepository.findById(classId).orElseThrow(() -> new NotFoundException("No such class: " + classId));
        AcademicYear academicYear = academicYearRepository
                .findById(academicYearId)
                .orElseThrow(() -> new NotFoundException("No such academic year: " + academicYearId));

        classTeacherAssignmentRepository
                .findBySchoolClassIdAndAcademicYearIdAndArchivedAtIsNull(classId, academicYearId)
                .ifPresent(
                        existing -> {
                            throw new BusinessRuleViolationException(
                                    "BR-AS-005", "This class already has a class teacher for this academic year");
                        });
        classTeacherAssignmentRepository
                .findByTeacherStaffIdAndAcademicYearIdAndArchivedAtIsNull(teacherStaffId, academicYearId)
                .ifPresent(
                        existing -> {
                            throw new BusinessRuleViolationException(
                                    "BR-AS-005", "This teacher is already class teacher of another class this year");
                        });

        return ClassTeacherAssignmentView.from(
                classTeacherAssignmentRepository.save(new ClassTeacherAssignment(schoolClass, academicYear, teacherStaffId)));
    }
}
