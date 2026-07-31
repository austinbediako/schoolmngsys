package com.drakalabs.schoolmngsys.finance.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** FR-FIN-06 arrears aging: one still-open invoice, how much it's short, and how long it's been outstanding. */
public record ArrearsEntry(UUID invoiceId, UUID enrollmentId, Instant issuedAt, BigDecimal balance, long daysOutstanding) {}
