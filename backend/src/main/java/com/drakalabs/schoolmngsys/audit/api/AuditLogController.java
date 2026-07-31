package com.drakalabs.schoolmngsys.audit.api;

import com.drakalabs.schoolmngsys.audit.service.AuditLogQueryService;
import com.drakalabs.schoolmngsys.audit.service.AuditLogView;
import com.drakalabs.schoolmngsys.shared.web.pagination.PageRequestFactory;
import com.drakalabs.schoolmngsys.shared.web.pagination.PageResponse;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditLogController {

    private final AuditLogQueryService auditLogQueryService;

    public AuditLogController(AuditLogQueryService auditLogQueryService) {
        this.auditLogQueryService = auditLogQueryService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT_VIEW')")
    public PageResponse<AuditLogResponse> listAuditLogs(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String entityId,
            @RequestParam(required = false) UUID actorAccountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequestFactory.of(page, size);
        Page<AuditLogView> views = auditLogQueryService.listAuditLogs(entityType, entityId, actorAccountId, fromDate, toDate, pageable);
        return PageResponse.from(views.map(AuditLogResponse::from));
    }
}
