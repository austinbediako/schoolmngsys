package com.drakalabs.schoolmngsys.auth.service;

import com.drakalabs.schoolmngsys.auth.domain.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Interim OTP delivery: logs it instead of sending SMS/email. Replace with an outbox-backed
 * sender when WP-8 (communication) exists — see docs/14 §8 "SMS provider selection".
 */
@Component
public class LoggingOtpSender implements OtpSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingOtpSender.class);

    @Override
    public void send(Account account, String otp) {
        log.warn(
                "[DEV-ONLY OTP DELIVERY] account={} otp={} — replace with the WP-8 SMS outbox before production",
                account.getId(),
                otp);
    }
}
