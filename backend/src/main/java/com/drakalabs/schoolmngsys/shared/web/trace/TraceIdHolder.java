package com.drakalabs.schoolmngsys.shared.web.trace;

import org.slf4j.MDC;

/** Per-request correlation id, set by {@link TraceIdFilter}, surfaced in problem responses. */
public final class TraceIdHolder {

    public static final String MDC_KEY = "traceId";

    private TraceIdHolder() {
    }

    public static String current() {
        String traceId = MDC.get(MDC_KEY);
        return traceId != null ? traceId : "unknown";
    }
}
