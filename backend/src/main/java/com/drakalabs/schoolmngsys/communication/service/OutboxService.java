package com.drakalabs.schoolmngsys.communication.service;

import com.drakalabs.schoolmngsys.communication.domain.MessageChannel;
import com.drakalabs.schoolmngsys.communication.domain.OutboxMessage;
import com.drakalabs.schoolmngsys.communication.repository.OutboxMessageRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxService {

    private final OutboxMessageRepository outboxMessageRepository;

    public OutboxService(OutboxMessageRepository outboxMessageRepository) {
        this.outboxMessageRepository = outboxMessageRepository;
    }

    @Transactional
    public OutboxMessageView enqueue(
            String templateCode,
            MessageChannel channel,
            String recipientType,
            UUID recipientId,
            String recipientPhone,
            String recipientEmail,
            String subject,
            String body,
            int maxRetries) {
        OutboxMessage outbox = new OutboxMessage(
                templateCode, channel, recipientType, recipientId, recipientPhone, recipientEmail, subject, body, maxRetries);
        return OutboxMessageView.from(outboxMessageRepository.save(outbox));
    }
}
