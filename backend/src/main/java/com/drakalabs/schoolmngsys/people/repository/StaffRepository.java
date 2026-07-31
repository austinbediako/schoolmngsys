package com.drakalabs.schoolmngsys.people.repository;

import com.drakalabs.schoolmngsys.people.domain.Staff;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<Staff, UUID> {

    Optional<Staff> findByStaffNumberAndArchivedAtIsNull(String staffNumber);

    long countByStaffNumberStartingWith(String prefix);
}
