package com.drakalabs.schoolmngsys.finance.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Per (Student, Term). {@code status} is a maintained cache, recomputed by the service layer
 * whenever an allocation or approved adjustment changes the balance — never hand-set.
 */
@Entity
@Table(name = "invoices")
public class Invoice extends BaseEntity {

    @Column(name = "enrollment_id", nullable = false, updatable = false)
    private UUID enrollmentId;

    @Column(name = "term_id", nullable = false, updatable = false)
    private UUID termId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvoiceStatus status = InvoiceStatus.ISSUED;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private Instant issuedAt;

    protected Invoice() {
    }

    public Invoice(UUID enrollmentId, UUID termId) {
        this.enrollmentId = enrollmentId;
        this.termId = termId;
        this.issuedAt = Instant.now();
    }

    public UUID getEnrollmentId() {
        return enrollmentId;
    }

    public UUID getTermId() {
        return termId;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void updateStatus(InvoiceStatus status) {
        this.status = status;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }
}
