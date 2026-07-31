package com.drakalabs.schoolmngsys.finance.service;

import com.drakalabs.schoolmngsys.finance.domain.Invoice;
import com.drakalabs.schoolmngsys.finance.domain.InvoiceStatus;
import com.drakalabs.schoolmngsys.finance.repository.InvoiceLineRepository;
import com.drakalabs.schoolmngsys.finance.repository.InvoiceRepository;
import com.drakalabs.schoolmngsys.finance.repository.PaymentAllocationRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * The one place that knows how to turn (invoice lines, payment allocations) into a balance and a
 * status — every service that changes either side (billing, payments, reversals, adjustments)
 * calls back through here rather than recomputing it independently.
 */
@Component
public class InvoiceLedgerService {

    private final InvoiceLineRepository invoiceLineRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;
    private final InvoiceRepository invoiceRepository;

    public InvoiceLedgerService(
            InvoiceLineRepository invoiceLineRepository,
            PaymentAllocationRepository paymentAllocationRepository,
            InvoiceRepository invoiceRepository) {
        this.invoiceLineRepository = invoiceLineRepository;
        this.paymentAllocationRepository = paymentAllocationRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount(Invoice invoice) {
        return invoiceLineRepository.findByInvoiceIdAndArchivedAtIsNull(invoice.getId()).stream()
                .map(com.drakalabs.schoolmngsys.finance.domain.InvoiceLine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Reversal allocations are negative rows, so a plain sum nets them out automatically. */
    @Transactional(readOnly = true)
    public BigDecimal allocatedAmount(Invoice invoice) {
        return paymentAllocationRepository.findByInvoiceIdAndArchivedAtIsNull(invoice.getId()).stream()
                .map(com.drakalabs.schoolmngsys.finance.domain.PaymentAllocation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public BigDecimal balance(Invoice invoice) {
        return totalAmount(invoice).subtract(allocatedAmount(invoice));
    }

    /** Recomputes and persists {@code invoice}'s status from its current lines/allocations. */
    @Transactional
    public void refreshStatus(Invoice invoice) {
        BigDecimal balance = balance(invoice);
        BigDecimal allocated = allocatedAmount(invoice);
        InvoiceStatus status;
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            status = InvoiceStatus.PAID;
        } else if (allocated.compareTo(BigDecimal.ZERO) > 0) {
            status = InvoiceStatus.PART_PAID;
        } else {
            status = InvoiceStatus.ISSUED;
        }
        invoice.updateStatus(status);
        invoiceRepository.save(invoice);
    }
}
