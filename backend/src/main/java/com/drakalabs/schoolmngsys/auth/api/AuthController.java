package com.drakalabs.schoolmngsys.auth.api;

import com.drakalabs.schoolmngsys.auth.service.AuthenticationService;
import com.drakalabs.schoolmngsys.auth.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthenticationService authenticationService, PasswordResetService passwordResetService) {
        this.authenticationService = authenticationService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody @Valid LoginRequest request, HttpServletRequest httpRequest) {
        return TokenResponse.from(
                authenticationService.login(request.identifier(), request.password(), httpRequest.getRemoteAddr()));
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestBody @Valid RefreshRequest request) {
        return TokenResponse.from(authenticationService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid RefreshRequest request) {
        authenticationService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<Void> requestPasswordReset(@RequestBody @Valid PasswordResetRequestDto request) {
        passwordResetService.requestReset(request.identifier());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> confirmPasswordReset(@RequestBody @Valid PasswordResetConfirmDto request) {
        passwordResetService.confirmReset(request.identifier(), request.otp(), request.newPassword());
        return ResponseEntity.noContent().build();
    }
}
