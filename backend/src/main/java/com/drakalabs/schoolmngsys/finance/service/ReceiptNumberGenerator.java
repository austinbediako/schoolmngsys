package com.drakalabs.schoolmngsys.finance.service;

import com.drakalabs.schoolmngsys.finance.repository.PaymentRepository;
import java.time.Clock;
import java.time.Year;
import org.springframework.stereotype.Component;

/** {@code RCP-<year>-<sequence>}, sequence zero-padded to 6 digits per year — mirrors StudentNumberGenerator's pattern. */
@Component
public class ReceiptNumberGenerator {

    private final PaymentRepository paymentRepository;
    private final Clock clock;

    public ReceiptNumberGenerator(PaymentRepository paymentRepository, Clock clock) {
        this.paymentRepository = paymentRepository;
        this.clock = clock;
    }

    public String generate() {
        String prefix = "RCP-" + Year.now(clock).getValue() + "-";
        long nextSequence = paymentRepository.countByReceiptNumberStartingWith(prefix) + 1;
        return prefix + String.format("%06d", nextSequence);
    }
}
