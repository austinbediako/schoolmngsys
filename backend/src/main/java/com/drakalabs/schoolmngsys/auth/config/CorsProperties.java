package com.drakalabs.schoolmngsys.auth.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code ubs.security.cors.*} — see {@link CorsConfig} for how these are applied.
 *
 * @param allowedOrigins Exact production origins (e.g. {@code https://staff.ubs-lmis.edu.gh}) —
 *     empty by default; never a wildcard, unlike {@code allowAnyLocalhostPort}.
 * @param allowAnyLocalhostPort Dev convenience: Vite dev servers pick their own port dynamically
 *     (5173 becomes 5174, 5175, ... whenever the preferred port is taken), so pinning exact dev
 *     origins here would break every time a port shifts. When true, any {@code http://localhost:*}
 *     or {@code http://127.0.0.1:*} origin is allowed — acceptable only because nothing but a
 *     developer's own machine can reach "localhost". Must be false wherever this isn't true (any
 *     real deployment).
 */
@ConfigurationProperties(prefix = "ubs.security.cors")
public record CorsProperties(List<String> allowedOrigins, boolean allowAnyLocalhostPort) {
}
