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

/** Append-only: a fee-item charge (positive) or an approved adjustment's reduction (negative) — never edited once billed. */
@Entity
@Table(name = "invoice_lines")
public class InvoiceLine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false, updatable = false)
    private Invoice invoice;

    @Column(name = "description", nullable = false, updatable = false)
    private String description;

    @Column(name = "amount", nullable = false, updatable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, updatable = false)
    private InvoiceLineSourceType sourceType;

    protected InvoiceLine() {
    }

    public InvoiceLine(Invoice invoice, String description, BigDecimal amount, InvoiceLineSourceType sourceType) {
        this.invoice = invoice;
        this.description = description;
        this.amount = amount;
        this.sourceType = sourceType;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public InvoiceLineSourceType getSourceType() {
        return sourceType;
    }
}
