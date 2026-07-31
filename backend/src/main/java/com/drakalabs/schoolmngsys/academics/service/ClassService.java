package com.drakalabs.schoolmngsys.academics.service;

import com.drakalabs.schoolmngsys.academics.domain.AcademicYear;
import com.drakalabs.schoolmngsys.academics.domain.ClassLevel;
import com.drakalabs.schoolmngsys.academics.domain.ClassTeacherAssignment;
import com.drakalabs.schoolmngsys.academics.domain.SchoolClass;
import com.drakalabs.schoolmngsys.academics.repository.AcademicYearRepository;
import com.drakalabs.schoolmngsys.academics.repository.ClassLevelRepository;
import com.drakalabs.schoolmngsys.academics.repository.ClassRepository;
import com.drakalabs.schoolmngsys.academics.repository.ClassTeacherAssignmentRepository;
import com.drakalabs.schoolmngsys.shared.audit.Audited;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Classes and per-year class-teacher assignment (BR-AS-004/005, A-01, FR-ACAD-03). */
@Service
public class ClassService {

    private final ClassRepository classRepository;
    private final ClassLevelRepository classLevelRepository;
    private final AcademicYearRepository academicYearRepository;
    private final ClassTeacherAssignmentRepository classTeacherAssignmentRepository;

    public ClassService(
            ClassRepository classRepository,
            ClassLevelRepository classLevelRepository,
            AcademicYearRepository academicYearRepository,
            ClassTeacherAssignmentRepository classTeacherAssignmentRepository) {
        this.classRepository = classRepository;
        this.classLevelRepository = classLevelRepository;
        this.academicYearRepository = academicYearRepository;
        this.classTeacherAssignmentRepository = classTeacherAssignmentRepository;
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

        return ClassView.from(classRepository.save(new SchoolClass(level, stream, capacity)));
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
