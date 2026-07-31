package com.drakalabs.schoolmngsys.finance.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * BR-FI-003: immutable once posted — {@code amount}/{@code channel}/{@code reference}/
 * {@code receiptNumber} are {@code updatable = false}, so Hibernate can never emit an UPDATE for
 * them even by accident (docs/14 §8: application-only enforcement, no DB trigger). Errors are
 * corrected exclusively by a new reversal {@link Payment} row (negative amount), never an edit.
 */
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

    @Column(name = "enrollment_id", nullable = false, updatable = false)
    private UUID enrollmentId;

    @Column(name = "amount", nullable = false, updatable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, updatable = false)
    private PaymentChannel channel;

    @Column(name = "reference", updatable = false)
    private String reference;

    @Column(name = "receipt_number", nullable = false, updatable = false)
    private String receiptNumber;

    @Column(name = "reversed", nullable = false)
    private boolean reversed;

    @Column(name = "reversal_of_payment_id", updatable = false)
    private UUID reversalOfPaymentId;

    @Column(name = "reversal_reason", updatable = false)
    private String reversalReason;

    @Column(name = "allocation_override_reason", updatable = false)
    private String allocationOverrideReason;

    protected Payment() {
    }

    private Payment(
            UUID enrollmentId,
            BigDecimal amount,
            PaymentChannel channel,
            String reference,
            String receiptNumber,
            UUID reversalOfPaymentId,
            String reversalReason,
            String allocationOverrideReason) {
        this.enrollmentId = enrollmentId;
        this.amount = amount;
        this.channel = channel;
        this.reference = reference;
        this.receiptNumber = receiptNumber;
        this.reversalOfPaymentId = reversalOfPaymentId;
        this.reversalReason = reversalReason;
        this.allocationOverrideReason = allocationOverrideReason;
    }

    public static Payment original(
            UUID enrollmentId, BigDecimal amount, PaymentChannel channel, String reference, String receiptNumber, String allocationOverrideReason) {
        return new Payment(enrollmentId, amount, channel, reference, receiptNumber, null, null, allocationOverrideReason);
    }

    public static Payment reversalOf(Payment original, String receiptNumber, String reversalReason) {
        return new Payment(
                original.enrollmentId,
                original.amount.negate(),
                original.channel,
                original.reference,
                receiptNumber,
                original.getId(),
                reversalReason,
                null);
    }

    public UUID getEnrollmentId() {
        return enrollmentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentChannel getChannel() {
        return channel;
    }

    public String getReference() {
        return reference;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public boolean isReversed() {
        return reversed;
    }

    public UUID getReversalOfPaymentId() {
        return reversalOfPaymentId;
    }

    public String getReversalReason() {
        return reversalReason;
    }

    public String getAllocationOverrideReason() {
        return allocationOverrideReason;
    }

    public boolean isReversal() {
        return reversalOfPaymentId != null;
    }

    public void markReversed() {
        this.reversed = true;
    }
}
