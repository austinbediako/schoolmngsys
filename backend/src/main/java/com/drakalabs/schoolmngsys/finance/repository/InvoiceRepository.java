package com.drakalabs.schoolmngsys.finance.repository;

import com.drakalabs.schoolmngsys.finance.domain.Invoice;
import com.drakalabs.schoolmngsys.finance.domain.InvoiceStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Optional<Invoice> findByEnrollmentIdAndTermIdAndArchivedAtIsNull(UUID enrollmentId, UUID termId);

    /** Oldest-first (BR-FI-002/A-09): ordered by issuedAt ascending, only invoices not yet fully paid. */
    List<Invoice> findByEnrollmentIdAndStatusNotAndArchivedAtIsNullOrderByIssuedAtAsc(UUID enrollmentId, InvoiceStatus excludedStatus);

    List<Invoice> findByEnrollmentIdAndArchivedAtIsNullOrderByIssuedAtDesc(UUID enrollmentId);
}
