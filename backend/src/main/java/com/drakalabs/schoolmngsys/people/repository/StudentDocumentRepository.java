package com.drakalabs.schoolmngsys.people.repository;

import com.drakalabs.schoolmngsys.people.domain.StudentDocument;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentDocumentRepository extends JpaRepository<StudentDocument, UUID> {

    List<StudentDocument> findByStudentIdAndArchivedAtIsNull(UUID studentId);
}
