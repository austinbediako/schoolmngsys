package com.drakalabs.schoolmngsys.people.service;

import com.drakalabs.schoolmngsys.people.domain.Guardian;
import com.drakalabs.schoolmngsys.people.domain.RelationshipType;
import com.drakalabs.schoolmngsys.people.domain.Student;
import com.drakalabs.schoolmngsys.people.domain.StudentGuardian;
import com.drakalabs.schoolmngsys.people.repository.GuardianRepository;
import com.drakalabs.schoolmngsys.people.repository.StudentGuardianRepository;
import com.drakalabs.schoolmngsys.people.repository.StudentRepository;
import com.drakalabs.schoolmngsys.shared.audit.Audited;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Adding/removing guardian links after student creation — BR-EN-004 is re-checked on removal. */
@Service
public class StudentGuardianLinkService {

    private final StudentRepository studentRepository;
    private final GuardianRepository guardianRepository;
    private final StudentGuardianRepository studentGuardianRepository;

    public StudentGuardianLinkService(
            StudentRepository studentRepository,
            GuardianRepository guardianRepository,
            StudentGuardianRepository studentGuardianRepository) {
        this.studentRepository = studentRepository;
        this.guardianRepository = guardianRepository;
        this.studentGuardianRepository = studentGuardianRepository;
    }

    @Audited(action = "GUARDIAN_LINKED", entityType = "StudentGuardian")
    @Transactional
    public StudentGuardianView link(
            UUID studentId,
            UUID guardianId,
            RelationshipType relationshipType,
            boolean primaryContact,
            boolean hasCustody,
            boolean receivesBilling,
            boolean receivesAcademicReports) {
        Student student =
                studentRepository.findById(studentId).orElseThrow(() -> new NotFoundException("No such student: " + studentId));
        Guardian guardian =
                guardianRepository.findById(guardianId).orElseThrow(() -> new NotFoundException("No such guardian: " + guardianId));

        studentGuardianRepository
                .findByStudentIdAndGuardianIdAndArchivedAtIsNull(studentId, guardianId)
                .ifPresent(
                        existing -> {
                            throw new BusinessRuleViolationException(
                                    "BR-EN-004", "This guardian is already linked to this student");
                        });

        return StudentGuardianView.from(
                studentGuardianRepository.save(
                        new StudentGuardian(
                                student, guardian, relationshipType, primaryContact, hasCustody, receivesBilling, receivesAcademicReports)));
    }

    /** BR-EN-004: refuses to remove the last guardian link, or the last primary-contact link. */
    @Audited(action = "GUARDIAN_UNLINKED", entityType = "StudentGuardian")
    @Transactional
    public void unlink(UUID studentId, UUID guardianId) {
        StudentGuardian link = studentGuardianRepository
                .findByStudentIdAndGuardianIdAndArchivedAtIsNull(studentId, guardianId)
                .orElseThrow(() -> new NotFoundException("No such guardian link for this student"));

        List<StudentGuardian> remainingAfterRemoval =
                studentGuardianRepository.findByStudentIdAndArchivedAtIsNull(studentId).stream()
                        .filter(existing -> !existing.getId().equals(link.getId()))
                        .toList();

        if (remainingAfterRemoval.isEmpty()) {
            throw new BusinessRuleViolationException("BR-EN-004", "A student must have at least one linked guardian");
        }
        if (remainingAfterRemoval.stream().noneMatch(StudentGuardian::isPrimaryContact)) {
            throw new BusinessRuleViolationException(
                    "BR-EN-004", "A student must have at least one primary-contact guardian");
        }

        link.archive();
        studentGuardianRepository.save(link);
    }
}
