package com.drakalabs.schoolmngsys.audit.service;

import com.drakalabs.schoolmngsys.audit.domain.AuditLog;
import com.drakalabs.schoolmngsys.audit.repository.AuditLogRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Writes the audit entry with {@link Propagation#MANDATORY}: BR-SE-002/ADR-007 require the audit
 * row to commit atomically with the mutation it describes, so this deliberately fails loudly
 * (IllegalTransactionStateException) if the annotated service method wasn't already running in a
 * transaction, rather than silently writing audit in a transaction of its own.
 */
@Component
public class AuditRecorder {

    private final AuditLogRepository auditLogRepository;

    public AuditRecorder(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(AuditLog entry) {
        auditLogRepository.save(entry);
    }
}
