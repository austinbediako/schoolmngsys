package com.drakalabs.schoolmngsys.finance.repository;

import com.drakalabs.schoolmngsys.finance.domain.InvoiceLine;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceLineRepository extends JpaRepository<InvoiceLine, UUID> {

    List<InvoiceLine> findByInvoiceIdAndArchivedAtIsNull(UUID invoiceId);
}
