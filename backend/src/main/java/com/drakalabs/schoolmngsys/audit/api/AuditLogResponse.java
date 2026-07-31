package com.drakalabs.schoolmngsys.audit.api;

import com.drakalabs.schoolmngsys.audit.service.AuditLogView;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        Instant occurredAt,
        UUID actorAccountId,
        String action,
        String entityType,
        String entityId,
        Map<String, Object> summary,
        String ip
) {

    public static AuditLogResponse from(AuditLogView view) {
        return new AuditLogResponse(
                view.id(),
                view.occurredAt(),
                view.actorAccountId(),
                view.action(),
                view.entityType(),
                view.entityId(),
                view.summary(),
                view.ip()
        );
    }
}
