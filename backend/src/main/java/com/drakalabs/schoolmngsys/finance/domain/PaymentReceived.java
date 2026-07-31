package com.drakalabs.schoolmngsys.finance.domain;

import com.drakalabs.schoolmngsys.shared.event.DomainEvent;
import java.math.BigDecimal;
import java.util.UUID;

/** FR-FIN-03/docs/02 §4: published on payment recording for receipt notifications. */
public class PaymentReceived extends DomainEvent {

    private final UUID paymentId;
    private final UUID enrollmentId;
    private final BigDecimal amount;
    private final String receiptNumber;

    public PaymentReceived(UUID paymentId, UUID enrollmentId, BigDecimal amount, String receiptNumber) {
        this.paymentId = paymentId;
        this.enrollmentId = enrollmentId;
        this.amount = amount;
        this.receiptNumber = receiptNumber;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public UUID getEnrollmentId() {
        return enrollmentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }
}
