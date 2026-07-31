package com.drakalabs.schoolmngsys.auth.repository;

import com.drakalabs.schoolmngsys.auth.domain.Permission;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
}
