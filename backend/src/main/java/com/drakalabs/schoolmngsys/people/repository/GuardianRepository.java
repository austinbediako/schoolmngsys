package com.drakalabs.schoolmngsys.people.repository;

import com.drakalabs.schoolmngsys.people.domain.Guardian;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuardianRepository extends JpaRepository<Guardian, UUID> {
}
