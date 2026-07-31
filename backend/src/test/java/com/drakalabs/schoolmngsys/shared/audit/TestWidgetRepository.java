package com.drakalabs.schoolmngsys.shared.audit;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface TestWidgetRepository extends JpaRepository<TestWidget, UUID> {
}
