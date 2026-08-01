package com.drakalabs.schoolmngsys.people.repository;

import com.drakalabs.schoolmngsys.people.domain.StaffDocument;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffDocumentRepository extends JpaRepository<StaffDocument, UUID> {

    List<StaffDocument> findByStaffIdAndArchivedAtIsNull(UUID staffId);
}
