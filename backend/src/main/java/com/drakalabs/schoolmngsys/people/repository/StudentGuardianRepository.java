package com.drakalabs.schoolmngsys.people.repository;

import com.drakalabs.schoolmngsys.people.domain.StudentGuardian;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentGuardianRepository extends JpaRepository<StudentGuardian, UUID> {

    List<StudentGuardian> findByStudentIdAndArchivedAtIsNull(UUID studentId);

    List<StudentGuardian> findByGuardianIdAndArchivedAtIsNull(UUID guardianId);

    Optional<StudentGuardian> findByStudentIdAndGuardianIdAndArchivedAtIsNull(UUID studentId, UUID guardianId);
}
