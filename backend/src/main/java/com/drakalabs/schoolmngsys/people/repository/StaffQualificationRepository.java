package com.drakalabs.schoolmngsys.people.repository;

import com.drakalabs.schoolmngsys.people.domain.StaffQualification;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffQualificationRepository extends JpaRepository<StaffQualification, UUID> {

    List<StaffQualification> findByStaffIdAndArchivedAtIsNull(UUID staffId);
}
