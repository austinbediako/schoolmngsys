package com.drakalabs.schoolmngsys.communication.service.provider;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Default SMS provider implementation logging messages (dev/test). */
@Component
public class LoggingSmsAdapter implements SmsAdapter {

    private static final Logger log = LoggerFactory.getLogger(LoggingSmsAdapter.class);

    @Override
    public SmsSendResult sendSms(String recipientPhone, String message) {
        String ref = "SMS-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("[SMS OUTBOUND] Recipient: {}, Ref: {}, Message: {}", recipientPhone, ref, message);
        return SmsSendResult.success(providerName(), ref);
    }

    @Override
    public String providerName() {
        return "LoggingSmsProvider";
    }
}
