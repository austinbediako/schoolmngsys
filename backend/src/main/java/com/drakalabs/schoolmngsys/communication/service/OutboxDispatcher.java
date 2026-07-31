package com.drakalabs.schoolmngsys.communication.service;

import com.drakalabs.schoolmngsys.communication.domain.DeliveryStatus;
import com.drakalabs.schoolmngsys.communication.domain.MessageChannel;
import com.drakalabs.schoolmngsys.communication.domain.NotificationDelivery;
import com.drakalabs.schoolmngsys.communication.domain.OutboxMessage;
import com.drakalabs.schoolmngsys.communication.domain.OutboxStatus;
import com.drakalabs.schoolmngsys.communication.repository.NotificationDeliveryRepository;
import com.drakalabs.schoolmngsys.communication.repository.OutboxMessageRepository;
import com.drakalabs.schoolmngsys.communication.service.provider.EmailAdapter;
import com.drakalabs.schoolmngsys.communication.service.provider.EmailSendResult;
import com.drakalabs.schoolmngsys.communication.service.provider.SmsAdapter;
import com.drakalabs.schoolmngsys.communication.service.provider.SmsSendResult;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outbox dispatcher service (ADR-008, NFR-08, BR-CO-004).
 * Periodically processes pending outbox notifications with retry, backoff, and delivery logging.
 */
@Service
public class OutboxDispatcher {

    private static final Logger log = LoggerFactory.getLogger(OutboxDispatcher.class);

    private final OutboxMessageRepository outboxMessageRepository;
    private final NotificationDeliveryRepository notificationDeliveryRepository;
    private final SmsAdapter smsAdapter;
    private final EmailAdapter emailAdapter;

    public OutboxDispatcher(
            OutboxMessageRepository outboxMessageRepository,
            NotificationDeliveryRepository notificationDeliveryRepository,
            SmsAdapter smsAdapter,
            EmailAdapter emailAdapter) {
        this.outboxMessageRepository = outboxMessageRepository;
        this.notificationDeliveryRepository = notificationDeliveryRepository;
        this.smsAdapter = smsAdapter;
        this.emailAdapter = emailAdapter;
    }

    @Scheduled(fixedDelayString = "${app.outbox.dispatch-interval-ms:10000}")
    public void scheduledDispatch() {
        processPending();
    }

    public int processPending() {
        List<OutboxMessage> pending = outboxMessageRepository.findPendingToProcess(OutboxStatus.PENDING, Instant.now());
        int processedCount = 0;
        for (OutboxMessage message : pending) {
            try {
                processSingleMessage(message.getId());
                processedCount++;
            } catch (Exception e) {
                log.error("Unexpected error dispatching outbox message {}: {}", message.getId(), e.getMessage(), e);
            }
        }
        return processedCount;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSingleMessage(UUID outboxId) {
        OutboxMessage message = outboxMessageRepository.findById(outboxId).orElse(null);
        if (message == null || message.getStatus() != OutboxStatus.PENDING) {
            return;
        }

        message.markProcessing();

        try {
            if (message.getChannel() == MessageChannel.SMS) {
                dispatchSms(message);
            } else if (message.getChannel() == MessageChannel.EMAIL) {
                dispatchEmail(message);
            } else {
                message.markSent("InAppProvider", "IN_APP-" + message.getId());
                outboxMessageRepository.save(message);
                recordDelivery(message, "InAppProvider", "IN_APP-" + message.getId(), DeliveryStatus.SUCCESS, null, BigDecimal.ZERO);
            }
        } catch (Exception e) {
            String errMsg = "Delivery exception: " + e.getMessage();
            log.warn("Failed to deliver outbox message {}: {}", outboxId, errMsg);
            Instant nextAttempt = Instant.now().plusSeconds((long) Math.pow(2, message.getRetryCount() + 1) * 5);
            message.recordFailure(errMsg, nextAttempt);
            outboxMessageRepository.save(message);
            recordDelivery(message, "UnknownProvider", null, DeliveryStatus.FAILED, errMsg, null);
        }
    }

    private void dispatchSms(OutboxMessage message) {
        String recipient = message.getRecipientPhone();
        if (recipient == null || recipient.isBlank()) {
            message.recordFailure("Recipient phone number missing", null);
            outboxMessageRepository.save(message);
            recordDelivery(message, smsAdapter.providerName(), null, DeliveryStatus.FAILED, "Recipient phone number missing", null);
            return;
        }

        SmsSendResult result = smsAdapter.sendSms(recipient, message.getBody());
        if (result.success()) {
            message.markSent(result.providerName(), result.providerReference());
            outboxMessageRepository.save(message);
            recordDelivery(message, result.providerName(), result.providerReference(), DeliveryStatus.SUCCESS, null, BigDecimal.ZERO);
        } else {
            Instant nextAttempt = Instant.now().plusSeconds((long) Math.pow(2, message.getRetryCount() + 1) * 5);
            message.recordFailure(result.errorMessage(), nextAttempt);
            outboxMessageRepository.save(message);
            recordDelivery(message, result.providerName(), null, DeliveryStatus.FAILED, result.errorMessage(), null);
        }
    }

    private void dispatchEmail(OutboxMessage message) {
        String recipient = message.getRecipientEmail();
        if (recipient == null || recipient.isBlank()) {
            message.recordFailure("Recipient email address missing", null);
            outboxMessageRepository.save(message);
            recordDelivery(message, emailAdapter.providerName(), null, DeliveryStatus.FAILED, "Recipient email address missing", null);
            return;
        }

        EmailSendResult result = emailAdapter.sendEmail(recipient, message.getSubject(), message.getBody());
        if (result.success()) {
            message.markSent(result.providerName(), result.providerReference());
            outboxMessageRepository.save(message);
            recordDelivery(message, result.providerName(), result.providerReference(), DeliveryStatus.SUCCESS, null, BigDecimal.ZERO);
        } else {
            Instant nextAttempt = Instant.now().plusSeconds((long) Math.pow(2, message.getRetryCount() + 1) * 5);
            message.recordFailure(result.errorMessage(), nextAttempt);
            outboxMessageRepository.save(message);
            recordDelivery(message, result.providerName(), null, DeliveryStatus.FAILED, result.errorMessage(), null);
        }
    }

    private void recordDelivery(
            OutboxMessage message, String providerName, String providerRef, DeliveryStatus status, String error, BigDecimal cost) {
        String recipient = message.getChannel() == MessageChannel.SMS ? message.getRecipientPhone() : message.getRecipientEmail();
        if (recipient == null) {
            recipient = "UNKNOWN";
        }
        NotificationDelivery delivery = new NotificationDelivery(
                message.getId(), message.getChannel(), recipient, providerName, providerRef, status, Instant.now(), error, cost);
        notificationDeliveryRepository.save(delivery);
    }
}
