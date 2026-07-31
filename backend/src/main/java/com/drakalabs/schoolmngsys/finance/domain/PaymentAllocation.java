package com.drakalabs.schoolmngsys.finance.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** How much of a {@link Payment} was applied to a given {@link Invoice} — oldest-first (BR-FI-002/A-09). */
@Entity
@Table(name = "payment_allocations")
public class PaymentAllocation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false, updatable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false, updatable = false)
    private Invoice invoice;

    @Column(name = "amount", nullable = false, updatable = false)
    private BigDecimal amount;

    protected PaymentAllocation() {
    }

    public PaymentAllocation(Payment payment, Invoice invoice, BigDecimal amount) {
        this.payment = payment;
        this.invoice = invoice;
        this.amount = amount;
    }

    public Payment getPayment() {
        return payment;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
