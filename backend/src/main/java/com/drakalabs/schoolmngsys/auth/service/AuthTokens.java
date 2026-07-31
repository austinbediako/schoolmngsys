package com.drakalabs.schoolmngsys.auth.service;

public record AuthTokens(String accessToken, String refreshToken, long expiresInSeconds) {
}
