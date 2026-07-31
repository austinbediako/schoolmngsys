package com.drakalabs.schoolmngsys.finance.repository;

import com.drakalabs.schoolmngsys.finance.domain.Adjustment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdjustmentRepository extends JpaRepository<Adjustment, UUID> {

    List<Adjustment> findByInvoiceIdAndArchivedAtIsNull(UUID invoiceId);
}
