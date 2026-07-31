package com.drakalabs.schoolmngsys.communication.service.provider;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Default Email provider implementation logging messages (dev/test). */
@Component
public class LoggingEmailAdapter implements EmailAdapter {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailAdapter.class);

    @Override
    public EmailSendResult sendEmail(String recipientEmail, String subject, String message) {
        String ref = "EMAIL-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("[EMAIL OUTBOUND] Recipient: {}, Subject: {}, Ref: {}, Message: {}", recipientEmail, subject, ref, message);
        return EmailSendResult.success(providerName(), ref);
    }

    @Override
    public String providerName() {
        return "LoggingEmailProvider";
    }
}
