package com.drakalabs.schoolmngsys.communication.service.provider;

/** Pluggable SMS provider interface (ADR-008, docs/14 §8). */
public interface SmsAdapter {
    SmsSendResult sendSms(String recipientPhone, String message);
    String providerName();
}
