package com.drakalabs.schoolmngsys.communication.service.provider;

public record SmsSendResult(
        boolean success,
        String providerName,
        String providerReference,
        String errorMessage
) {
    public static SmsSendResult success(String providerName, String providerReference) {
        return new SmsSendResult(true, providerName, providerReference, null);
    }

    public static SmsSendResult failure(String providerName, String errorMessage) {
        return new SmsSendResult(false, providerName, null, errorMessage);
    }
}
