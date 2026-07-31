package com.drakalabs.schoolmngsys.finance.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * BR-FI-004: a student-specific price change (discount/scholarship/waiver) with a reason — never
 * an edit to the fee schedule or an existing invoice line. Only takes effect (creates its own
 * {@link InvoiceLine}) once APPROVED (by the Head; ACCOUNTANT can only propose).
 */
@Entity
@Table(name = "adjustments")
public class Adjustment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false, updatable = false)
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = false)
    private AdjustmentType type;

    @Column(name = "amount", nullable = false, updatable = false)
    private BigDecimal amount;

    @Column(name = "reason", nullable = false, updatable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AdjustmentStatus status = AdjustmentStatus.PROPOSED;

    @Column(name = "invoice_line_id")
    private UUID invoiceLineId;

    protected Adjustment() {
    }

    public Adjustment(Invoice invoice, AdjustmentType type, BigDecimal amount, String reason) {
        this.invoice = invoice;
        this.type = type;
        this.amount = amount;
        this.reason = reason;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public AdjustmentType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getReason() {
        return reason;
    }

    public AdjustmentStatus getStatus() {
        return status;
    }

    public UUID getInvoiceLineId() {
        return invoiceLineId;
    }

    public void approve(UUID invoiceLineId) {
        this.status = AdjustmentStatus.APPROVED;
        this.invoiceLineId = invoiceLineId;
    }

    public void reject() {
        this.status = AdjustmentStatus.REJECTED;
    }
}
