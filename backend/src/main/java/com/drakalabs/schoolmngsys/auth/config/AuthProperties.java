package com.drakalabs.schoolmngsys.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(int lockoutThreshold, long lockoutDurationMs, long otpExpirationMs) {
}
