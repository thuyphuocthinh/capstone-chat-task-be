package com.tpt.chat_task.modules.auth.controller;

import com.tpt.chat_task.common.dto.SuccessResponse;
import com.tpt.chat_task.common.dto.SuccessResponseWithMessage;
import com.tpt.chat_task.modules.auth.dto.request.*;
import com.tpt.chat_task.modules.auth.service.AuthService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> loginHandler(
            @RequestBody @Valid LoginRequest loginRequest,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            @RequestHeader(value = "X-Forwarded-For", required = false) Optional<String> forwardedForOpt,
            HttpServletRequest request
    ) throws BadRequestException {
        String ip = forwardedForOpt.filter(f -> !f.isEmpty()).orElse(request.getRemoteAddr());

        SuccessResponse response = SuccessResponse.builder()
                .data(this.authService.login(loginRequest, ip, userAgent))
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerHandler(
            @RequestBody @Valid RegisterRequest request
            ) throws MessagingException, IOException {
        SuccessResponse response = SuccessResponse.builder()
                .data(this.authService.register(request))
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutHandler(
            @RequestBody @Valid LogoutRequest logoutRequest
    ) {
        SuccessResponseWithMessage response = SuccessResponseWithMessage.builder()
                .message(this.authService.logout(logoutRequest))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmailHandler(
            @RequestParam(name = "otp") String otp,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            @RequestHeader(value = "X-Forwarded-For", required = false) Optional<String> forwardedForOpt,
            HttpServletRequest request
    ) throws BadRequestException {
        String ip = forwardedForOpt.filter(f -> !f.isEmpty()).orElse(request.getRemoteAddr());

        SuccessResponse response = SuccessResponse.builder()
                .data(this.authService.verifyOtp(otp, ip, userAgent))
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshTokenHandler(
            @RequestBody @Valid RefreshTokenRequest refreshTokenRequest,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            @RequestHeader(value = "X-Forwarded-For", required = false) Optional<String> forwardedForOpt,
            HttpServletRequest request
    ) {
        String ip = forwardedForOpt.filter(f -> !f.isEmpty()).orElse(request.getRemoteAddr());

        SuccessResponse response = SuccessResponse.builder()
                .data(this.authService.refreshToken(refreshTokenRequest, ip, userAgent))
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPasswordHandler(
            @RequestBody @Valid ForgotPasswordRequest request
            ) throws MessagingException, IOException {
        SuccessResponseWithMessage response = SuccessResponseWithMessage.builder()
                .message(this.authService.forgotPassword(request))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtpHandler(
            @RequestBody @Valid VerifyOtpRequest request
    ) throws MessagingException, IOException {
        SuccessResponseWithMessage response = SuccessResponseWithMessage.builder()
                .message(this.authService.verifyOtpBeforeReset(request))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/reset-password")
    public ResponseEntity<?> resetPasswordHandler(
            @RequestBody @Valid ResetPasswordRequest request
    ) throws MessagingException, IOException {
        SuccessResponseWithMessage response = SuccessResponseWithMessage.builder()
                .message(this.authService.resetPassword(request))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
