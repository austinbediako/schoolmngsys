package com.drakalabs.schoolmngsys.communication.domain;

public enum OutboxStatus {
    PENDING,
    PROCESSING,
    SENT,
    FAILED
}
