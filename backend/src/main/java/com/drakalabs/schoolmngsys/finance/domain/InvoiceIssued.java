package com.drakalabs.schoolmngsys.finance.domain;

import com.drakalabs.schoolmngsys.shared.event.DomainEvent;
import java.math.BigDecimal;
import java.util.UUID;

/** FR-FIN-02/docs/02 §4: published on billing run completion for outbox notifications. */
public class InvoiceIssued extends DomainEvent {

    private final UUID invoiceId;
    private final UUID enrollmentId;
    private final UUID termId;
    private final BigDecimal totalAmount;

    public InvoiceIssued(UUID invoiceId, UUID enrollmentId, UUID termId, BigDecimal totalAmount) {
        this.invoiceId = invoiceId;
        this.enrollmentId = enrollmentId;
        this.termId = termId;
        this.totalAmount = totalAmount;
    }

    public UUID getInvoiceId() {
        return invoiceId;
    }

    public UUID getEnrollmentId() {
        return enrollmentId;
    }

    public UUID getTermId() {
        return termId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}
