package com.drakalabs.schoolmngsys.assessment.domain;

/** BR-AA-003: DRAFT (teacher) -> SUBMITTED (teacher locks) -> HOD_APPROVED -> PUBLISHED (Head). */
public enum ResultStatus {
    DRAFT,
    SUBMITTED,
    HOD_APPROVED,
    PUBLISHED
}
