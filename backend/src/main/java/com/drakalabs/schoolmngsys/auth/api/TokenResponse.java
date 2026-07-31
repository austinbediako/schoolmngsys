package com.drakalabs.schoolmngsys.auth.api;

import com.drakalabs.schoolmngsys.auth.service.AuthTokens;

public record TokenResponse(String accessToken, String refreshToken, long expiresInSeconds) {

    public static TokenResponse from(AuthTokens tokens) {
        return new TokenResponse(tokens.accessToken(), tokens.refreshToken(), tokens.expiresInSeconds());
    }
}
