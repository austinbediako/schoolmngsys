package com.drakalabs.schoolmngsys.communication.api;

import com.drakalabs.schoolmngsys.communication.service.CommunicationQueryService;
import com.drakalabs.schoolmngsys.communication.service.OutboxDispatcher;
import com.drakalabs.schoolmngsys.shared.security.CurrentAccountProvider;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import com.drakalabs.schoolmngsys.shared.web.pagination.PageRequestFactory;

import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final CommunicationQueryService communicationQueryService;
    private final OutboxDispatcher outboxDispatcher;
    private final CurrentAccountProvider currentAccountProvider;

    public NotificationController(
            CommunicationQueryService communicationQueryService,
            OutboxDispatcher outboxDispatcher,
            CurrentAccountProvider currentAccountProvider) {
        this.communicationQueryService = communicationQueryService;
        this.outboxDispatcher = outboxDispatcher;
        this.currentAccountProvider = currentAccountProvider;
    }

    /** Full, unfiltered outbox — audit/reporting only (BR-CO-004). Never expose this to NOTIFICATION_VIEW_OWN callers. */
    @GetMapping("/outbox")
    @PreAuthorize("hasAuthority('AUDIT_VIEW')")
    public Page<OutboxMessageResponse> listOutbox(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequestFactory.of(page, size);
        return communicationQueryService.listOutbox(pageable).map(OutboxMessageResponse::from);
    }

    /** Scope-filtered to the caller's own messages (docs/11 §3) — the only listing a guardian/student may reach. */
    @GetMapping("/my-outbox")
    @PreAuthorize("hasAuthority('NOTIFICATION_VIEW_OWN')")
    public Page<OutboxMessageResponse> listMyOutbox(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var recipientId = currentAccountProvider
                .current()
                .map(account -> account.personId())
                .orElseThrow(() -> new NotFoundException("No linked person record for this account"));
        Pageable pageable = PageRequestFactory.of(page, size);
        return communicationQueryService.listOutboxForRecipient(recipientId, pageable).map(OutboxMessageResponse::from);
    }

    @GetMapping("/deliveries")
    @PreAuthorize("hasAuthority('AUDIT_VIEW')")
    public Page<NotificationDeliveryResponse> listDeliveries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequestFactory.of(page, size);
        return communicationQueryService.listDeliveries(pageable).map(NotificationDeliveryResponse::from);
    }

    @PostMapping("/outbox/process")
    @PreAuthorize("hasAuthority('ANNOUNCEMENT_CREATE') or hasAuthority('ACCOUNT_UPDATE')")
    public Map<String, Object> triggerProcessOutbox() {
        int processed = outboxDispatcher.processPending();
        return Map.of("processed", processed, "status", "SUCCESS");
    }
}
