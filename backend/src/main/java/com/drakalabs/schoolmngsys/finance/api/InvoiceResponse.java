package com.drakalabs.schoolmngsys.finance.api;

import com.drakalabs.schoolmngsys.finance.domain.InvoiceStatus;
import com.drakalabs.schoolmngsys.finance.service.InvoiceView;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        UUID enrollmentId,
        UUID termId,
        InvoiceStatus status,
        Instant issuedAt,
        BigDecimal totalAmount,
        BigDecimal balance,
        List<InvoiceLineResponse> lines) {

    public static InvoiceResponse from(InvoiceView view) {
        return new InvoiceResponse(
                view.id(),
                view.enrollmentId(),
                view.termId(),
                view.status(),
                view.issuedAt(),
                view.totalAmount(),
                view.balance(),
                view.lines().stream().map(InvoiceLineResponse::from).toList());
    }
}
