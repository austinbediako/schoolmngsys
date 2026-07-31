package com.drakalabs.schoolmngsys.audit.service;

import com.drakalabs.schoolmngsys.audit.domain.AuditLog;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditLogView(
        UUID id,
        Instant occurredAt,
        UUID actorAccountId,
        String action,
        String entityType,
        String entityId,
        Map<String, Object> summary,
        String ip
) {

    public static AuditLogView from(AuditLog log) {
        return new AuditLogView(
                log.getId(),
                log.getOccurredAt(),
                log.getActorAccountId(),
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getSummary(),
                log.getIp()
        );
    }
}
