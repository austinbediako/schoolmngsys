package com.drakalabs.schoolmngsys.finance.repository;

import com.drakalabs.schoolmngsys.finance.domain.PaymentAllocation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentAllocationRepository extends JpaRepository<PaymentAllocation, UUID> {

    List<PaymentAllocation> findByPaymentIdAndArchivedAtIsNull(UUID paymentId);

    List<PaymentAllocation> findByInvoiceIdAndArchivedAtIsNull(UUID invoiceId);
}
