package com.drakalabs.schoolmngsys.shared.web.error;

/**
 * Thrown by service-layer semantic validation (docs/10 §3) when a request violates a documented
 * business rule. {@code ruleId} must cite a BR- id from docs/04 so the response is self-documenting.
 */
public class BusinessRuleViolationException extends RuntimeException {

    private final String ruleId;

    public BusinessRuleViolationException(String ruleId, String message) {
        super(message);
        this.ruleId = ruleId;
    }

    public String getRuleId() {
        return ruleId;
    }
}
