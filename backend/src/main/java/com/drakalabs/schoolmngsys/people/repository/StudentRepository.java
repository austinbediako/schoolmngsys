package com.drakalabs.schoolmngsys.people.repository;

import com.drakalabs.schoolmngsys.people.domain.Student;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, UUID> {

    Optional<Student> findByStudentNumberAndArchivedAtIsNull(String studentNumber);

    long countByStudentNumberStartingWith(String prefix);
}
