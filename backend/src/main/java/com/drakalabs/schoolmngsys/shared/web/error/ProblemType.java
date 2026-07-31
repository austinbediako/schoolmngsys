package com.drakalabs.schoolmngsys.shared.web.error;

import java.net.URI;
import org.springframework.http.HttpStatus;

/**
 * The stable problem-type catalog (docs/10 §2) — canonical {@code type} slugs and their
 * default HTTP status. Business-rule rejections use {@link #RULE_VIOLATION} and additionally
 * carry a {@code ruleId} property citing the BR- id.
 */
public enum ProblemType {

    VALIDATION("validation", "Validation failed", HttpStatus.BAD_REQUEST),
    NOT_FOUND("not-found", "Resource not found", HttpStatus.NOT_FOUND),
    CONFLICT("conflict", "Conflict", HttpStatus.CONFLICT),
    RULE_VIOLATION("rule-violation", "Business rule violation", HttpStatus.UNPROCESSABLE_ENTITY),
    AUTH_REQUIRED("auth-required", "Authentication required", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("forbidden", "Forbidden", HttpStatus.FORBIDDEN),
    RATE_LIMITED("rate-limited", "Rate limited", HttpStatus.TOO_MANY_REQUESTS),
    INTERNAL("internal", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);

    private static final String BASE_URI = "https://ubs-lmis.example/problems/";

    private final String slug;
    private final String title;
    private final HttpStatus defaultStatus;

    ProblemType(String slug, String title, HttpStatus defaultStatus) {
        this.slug = slug;
        this.title = title;
        this.defaultStatus = defaultStatus;
    }

    public URI uri() {
        return URI.create(BASE_URI + slug);
    }

    public String title() {
        return title;
    }

    public HttpStatus defaultStatus() {
        return defaultStatus;
    }
}
