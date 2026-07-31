package com.drakalabs.schoolmngsys.finance.service;

import com.drakalabs.schoolmngsys.finance.domain.Invoice;
import com.drakalabs.schoolmngsys.finance.domain.InvoiceStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InvoiceView(
        UUID id,
        UUID enrollmentId,
        UUID termId,
        InvoiceStatus status,
        Instant issuedAt,
        BigDecimal totalAmount,
        BigDecimal balance,
        List<InvoiceLineView> lines) {

    public static InvoiceView from(Invoice invoice, List<InvoiceLineView> lines, BigDecimal totalAmount, BigDecimal balance) {
        return new InvoiceView(invoice.getId(), invoice.getEnrollmentId(), invoice.getTermId(), invoice.getStatus(), invoice.getIssuedAt(), totalAmount, balance, lines);
    }
}
