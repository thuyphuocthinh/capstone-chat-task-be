package com.tpt.chat_task.modules.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpt.chat_task.common.dto.SuccessResponse;
import com.tpt.chat_task.common.dto.SuccessResponseWithMessage;
import com.tpt.chat_task.modules.auth.constant.AuthError;
import com.tpt.chat_task.modules.auth.dto.request.*;
import com.tpt.chat_task.modules.auth.dto.response.LoginResponse;
import com.tpt.chat_task.modules.auth.oauth.OauthProvider;
import com.tpt.chat_task.modules.auth.service.AuthService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    private final OauthProvider oauthProvider;

    private final ObjectMapper objectMapper;

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
        SuccessResponse response = SuccessResponse.builder()
                .data(this.authService.verifyOtpBeforeReset(request))
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

    @PostMapping("/google/verify-token")
    public ResponseEntity<?> googleVerifyTokenHandler(
            @RequestBody GoogleTokenRequest googleTokenRequest,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            @RequestHeader(value = "X-Forwarded-For", required = false) Optional<String> forwardedForOpt,
            HttpServletRequest request
    ) throws IOException, MessagingException {
        GoogleUserDTO googleUserDTO = this.oauthProvider.verifyToken(googleTokenRequest.getToken());
        String ip = forwardedForOpt.filter(f -> !f.isEmpty()).orElse(request.getRemoteAddr());
        SuccessResponse response = SuccessResponse.builder()
                .data(this.authService.googleLogin(googleUserDTO, ip, userAgent))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/google/callback")
    public void googleCallbackHandler(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String state,
            HttpServletResponse response
    ) throws IOException, MessagingException {
        if (error != null || code == null) {
            throw new BadCredentialsException(AuthError.GOOGLE_LOGIN_FAILED);
        }

        GoogleUserDTO googleUserDTO = this.oauthProvider.getUserInfoFromGoogle(code);
        Map<String, Object> stateData;
        try {
            String decodedState = new String(Base64.getDecoder().decode(state));
            stateData = objectMapper.readValue(decodedState, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("Invalid state data: {}", state);
            response.sendRedirect("https://your-frontend.com/login-failed");
            throw new BadRequestException(AuthError.INVALID_GOOGLE_STATE_DATA);
        }

        // state tu FE phai bao gom ca user agent va ip
        String userAgent = (String) stateData.get("userAgent");
        String ip = (String) stateData.get("ip");

        LoginResponse loginResponse = this.authService.googleLogin(googleUserDTO, ip, userAgent);
        String accessToken = loginResponse.getAccessToken();
        String refreshToken = loginResponse.getRefreshToken();

        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
        accessTokenCookie.setHttpOnly(false);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(60 * 15);

        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(false);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(60 * 60 * 24 * 3);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        response.sendRedirect("https://your-frontend.com/auth/callback");
    }
}
