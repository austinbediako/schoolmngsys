package com.drakalabs.schoolmngsys.shared.api;

import java.time.Instant;

public record SystemStatusResponse(
        String application,
        String version,
        String status,
        Instant timestamp
) {
}
