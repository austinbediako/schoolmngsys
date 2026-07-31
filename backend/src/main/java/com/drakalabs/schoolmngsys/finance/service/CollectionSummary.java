package com.drakalabs.schoolmngsys.finance.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** FR-FIN-06 daily cash book: every non-reversal, non-reversed payment posted in a window, and their total. */
public record CollectionSummary(Instant from, Instant to, BigDecimal totalCollected, List<PaymentView> payments) {}
