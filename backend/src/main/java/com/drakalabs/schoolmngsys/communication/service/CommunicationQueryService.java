package com.drakalabs.schoolmngsys.communication.service;

import com.drakalabs.schoolmngsys.communication.repository.NotificationDeliveryRepository;
import com.drakalabs.schoolmngsys.communication.repository.OutboxMessageRepository;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommunicationQueryService {

    private final OutboxMessageRepository outboxMessageRepository;
    private final NotificationDeliveryRepository notificationDeliveryRepository;

    public CommunicationQueryService(
            OutboxMessageRepository outboxMessageRepository,
            NotificationDeliveryRepository notificationDeliveryRepository) {
        this.outboxMessageRepository = outboxMessageRepository;
        this.notificationDeliveryRepository = notificationDeliveryRepository;
    }

    @Transactional(readOnly = true)
    public Page<OutboxMessageView> listOutbox(Pageable pageable) {
        return outboxMessageRepository.findAll(pageable).map(OutboxMessageView::from);
    }

    /** Scope-filtered by the caller's own {@code recipientId} — the only listing NOTIFICATION_VIEW_OWN may reach. */
    @Transactional(readOnly = true)
    public Page<OutboxMessageView> listOutboxForRecipient(UUID recipientId, Pageable pageable) {
        return outboxMessageRepository.findByRecipientId(recipientId, pageable).map(OutboxMessageView::from);
    }

    @Transactional(readOnly = true)
    public OutboxMessageView getOutbox(UUID id) {
        return outboxMessageRepository
                .findById(id)
                .map(OutboxMessageView::from)
                .orElseThrow(() -> new NotFoundException("No such outbox message: " + id));
    }

    @Transactional(readOnly = true)
    public Page<NotificationDeliveryView> listDeliveries(Pageable pageable) {
        return notificationDeliveryRepository.findAll(pageable).map(NotificationDeliveryView::from);
    }

    @Transactional(readOnly = true)
    public List<NotificationDeliveryView> listDeliveriesForOutbox(UUID outboxId) {
        return notificationDeliveryRepository.findByOutboxIdOrderByAttemptedAtDesc(outboxId).stream()
                .map(NotificationDeliveryView::from)
                .toList();
    }
}
