package com.drakalabs.schoolmngsys.academics.repository;

import com.drakalabs.schoolmngsys.academics.domain.Department;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {
}
