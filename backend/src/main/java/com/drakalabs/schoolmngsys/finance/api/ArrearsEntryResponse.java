package com.drakalabs.schoolmngsys.finance.api;

import com.drakalabs.schoolmngsys.finance.service.ArrearsEntry;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ArrearsEntryResponse(UUID invoiceId, UUID enrollmentId, Instant issuedAt, BigDecimal balance, long daysOutstanding) {

    public static ArrearsEntryResponse from(ArrearsEntry entry) {
        return new ArrearsEntryResponse(entry.invoiceId(), entry.enrollmentId(), entry.issuedAt(), entry.balance(), entry.daysOutstanding());
    }
}
