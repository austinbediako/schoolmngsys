package com.drakalabs.schoolmngsys.assessment.repository;

import com.drakalabs.schoolmngsys.assessment.domain.DefaultGradeBand;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DefaultGradeBandRepository extends JpaRepository<DefaultGradeBand, UUID> {
}
