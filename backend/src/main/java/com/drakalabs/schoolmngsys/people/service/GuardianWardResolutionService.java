package com.drakalabs.schoolmngsys.people.service;

import com.drakalabs.schoolmngsys.people.domain.Student;
import com.drakalabs.schoolmngsys.people.domain.StudentGuardian;
import com.drakalabs.schoolmngsys.people.repository.StudentGuardianRepository;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The authorization scope source docs/08 §4 names explicitly: "guardian-ward resolution". Every
 * future module's scope filter for a GUARDIAN-role caller (attendance, results, finance, ...)
 * answers "which students can this guardian see?" by calling this — never by querying
 * {@code student_guardians} directly, since that repository is private to {@code people}.
 */
@Service
public class GuardianWardResolutionService {

    private final StudentGuardianRepository studentGuardianRepository;

    public GuardianWardResolutionService(StudentGuardianRepository studentGuardianRepository) {
        this.studentGuardianRepository = studentGuardianRepository;
    }

    @Transactional(readOnly = true)
    public Set<UUID> resolveWardIds(UUID guardianId) {
        return studentGuardianRepository.findByGuardianIdAndArchivedAtIsNull(guardianId).stream()
                .map(StudentGuardian::getStudent)
                .map(Student::getId)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Transactional(readOnly = true)
    public boolean isWardOf(UUID guardianId, UUID studentId) {
        return resolveWardIds(guardianId).contains(studentId);
    }
}
