package com.drakalabs.schoolmngsys.finance.repository;

import com.drakalabs.schoolmngsys.finance.domain.Payment;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByReceiptNumber(String receiptNumber);

    long countByReceiptNumberStartingWith(String prefix);

    List<Payment> findByEnrollmentIdOrderByCreatedAtDesc(UUID enrollmentId);

    List<Payment> findByCreatedAtBetweenOrderByCreatedAtAsc(Instant from, Instant to);
}
