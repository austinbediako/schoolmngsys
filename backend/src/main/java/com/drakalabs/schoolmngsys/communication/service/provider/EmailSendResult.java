package com.drakalabs.schoolmngsys.communication.service.provider;

public record EmailSendResult(
        boolean success,
        String providerName,
        String providerReference,
        String errorMessage
) {
    public static EmailSendResult success(String providerName, String providerReference) {
        return new EmailSendResult(true, providerName, providerReference, null);
    }

    public static EmailSendResult failure(String providerName, String errorMessage) {
        return new EmailSendResult(false, providerName, null, errorMessage);
    }
}
