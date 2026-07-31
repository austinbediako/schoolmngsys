package com.drakalabs.schoolmngsys.auth.repository;

import com.drakalabs.schoolmngsys.auth.domain.Role;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByNameAndArchivedAtIsNull(String name);
}
