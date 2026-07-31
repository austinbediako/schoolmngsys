package com.drakalabs.schoolmngsys.people.service;

import com.drakalabs.schoolmngsys.people.domain.Gender;
import com.drakalabs.schoolmngsys.people.domain.Guardian;
import com.drakalabs.schoolmngsys.people.domain.Student;
import com.drakalabs.schoolmngsys.people.domain.StudentGuardian;
import com.drakalabs.schoolmngsys.people.repository.GuardianRepository;
import com.drakalabs.schoolmngsys.people.repository.StudentGuardianRepository;
import com.drakalabs.schoolmngsys.people.repository.StudentRepository;
import com.drakalabs.schoolmngsys.shared.audit.Audited;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Student bio-data and BR-EN-004 (every ACTIVE student needs >= 1 guardian link, >= 1 of them
 * primary contact) — enforced atomically at creation so a student is never briefly guardian-less.
 */
@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final GuardianRepository guardianRepository;
    private final StudentGuardianRepository studentGuardianRepository;
    private final StudentNumberGenerator studentNumberGenerator;

    public StudentService(
            StudentRepository studentRepository,
            GuardianRepository guardianRepository,
            StudentGuardianRepository studentGuardianRepository,
            StudentNumberGenerator studentNumberGenerator) {
        this.studentRepository = studentRepository;
        this.guardianRepository = guardianRepository;
        this.studentGuardianRepository = studentGuardianRepository;
        this.studentNumberGenerator = studentNumberGenerator;
    }

    @Audited(action = "STUDENT_CREATED", entityType = "Student")
    @Transactional
    public StudentView createStudent(
            String firstName,
            String lastName,
            String otherNames,
            LocalDate dateOfBirth,
            Gender gender,
            LocalDate admissionDate,
            List<GuardianLinkSpec> initialGuardianLinks) {
        if (initialGuardianLinks.isEmpty() || initialGuardianLinks.stream().noneMatch(GuardianLinkSpec::primaryContact)) {
            throw new BusinessRuleViolationException(
                    "BR-EN-004", "A student needs at least one linked guardian, with at least one marked primary contact");
        }

        String studentNumber = studentNumberGenerator.generate(admissionDate.getYear());
        Student student = new Student(studentNumber, firstName, lastName, otherNames, dateOfBirth, gender, admissionDate);
        studentRepository.save(student);

        for (GuardianLinkSpec spec : initialGuardianLinks) {
            Guardian guardian = guardianRepository
                    .findById(spec.guardianId())
                    .orElseThrow(() -> new NotFoundException("No such guardian: " + spec.guardianId()));
            studentGuardianRepository.save(
                    new StudentGuardian(
                            student,
                            guardian,
                            spec.relationshipType(),
                            spec.primaryContact(),
                            spec.hasCustody(),
                            spec.receivesBilling(),
                            spec.receivesAcademicReports()));
        }

        return StudentView.from(student);
    }

    @Audited(action = "STUDENT_UPDATED", entityType = "Student")
    @Transactional
    public StudentView updateBio(UUID studentId, String firstName, String lastName, String otherNames) {
        Student student = getStudent(studentId);
        student.updateBio(firstName, lastName, otherNames);
        return StudentView.from(studentRepository.save(student));
    }

    @Audited(action = "STUDENT_ARCHIVED", entityType = "Student")
    @Transactional
    public StudentView archiveStudent(UUID studentId) {
        Student student = getStudent(studentId);
        student.archive();
        return StudentView.from(studentRepository.save(student));
    }

    /** BR-EN-005: called by {@code enrollment}'s exit workflow, which owns reason/date (docs/08 §3 ENR->PPL). */
    @Audited(action = "STUDENT_TRANSFERRED_OUT", entityType = "Student")
    @Transactional
    public StudentView markTransferredOut(UUID studentId) {
        Student student = getStudent(studentId);
        student.transferOut();
        return StudentView.from(studentRepository.save(student));
    }

    /** BR-EN-005: called by {@code enrollment}'s exit workflow, which owns reason/date (docs/08 §3 ENR->PPL). */
    @Audited(action = "STUDENT_WITHDRAWN", entityType = "Student")
    @Transactional
    public StudentView markWithdrawn(UUID studentId) {
        Student student = getStudent(studentId);
        student.withdraw();
        return StudentView.from(studentRepository.save(student));
    }

    private Student getStudent(UUID studentId) {
        return studentRepository.findById(studentId).orElseThrow(() -> new NotFoundException("No such student: " + studentId));
    }
}
