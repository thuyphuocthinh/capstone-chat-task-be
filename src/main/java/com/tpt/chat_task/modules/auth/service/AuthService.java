package com.tpt.chat_task.modules.auth.service;

import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.auth.dto.request.*;
import com.tpt.chat_task.modules.auth.dto.response.LoginResponse;
import com.tpt.chat_task.modules.auth.dto.response.VerifyOtpResponse;
import jakarta.mail.MessagingException;
import org.apache.coyote.BadRequestException;

import java.io.IOException;

public interface AuthService {
    public LoginResponse login(LoginRequest loginRequest, String userAgent, String ipAddress) throws BadRequestException;
    public String register(RegisterRequest registerRequest) throws NotFoundException, IOException, MessagingException;
    public LoginResponse verifyOtp(String otp, String ipAddress, String userAgent) throws NotFoundException, BadRequestException;
    public LoginResponse refreshToken(RefreshTokenRequest refreshTokenRequest, String ipAddress, String userAgent) throws NotFoundException;
    public String logout(LogoutRequest logoutRequest) throws NotFoundException;
    public String forgotPassword(ForgotPasswordRequest forgotPasswordRequest) throws NotFoundException, IOException, MessagingException;
    public VerifyOtpResponse verifyOtpBeforeReset(VerifyOtpRequest verifyOtpRequest) throws NotFoundException, IOException, MessagingException;
    public String resetPassword(ResetPasswordRequest resetPasswordRequest) throws NotFoundException, IOException, MessagingException;
    public LoginResponse googleLogin(GoogleUserDTO googleUserDTO, String userAgent, String ipAddress) throws NotFoundException, IOException, MessagingException;
}
