package com.tpt.chat_task.modules.auth.service.impl;

import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.infrastructure.email.service.EmailService;
import com.tpt.chat_task.infrastructure.redis.repository.CacheBlackList;
import com.tpt.chat_task.modules.auth.constant.AuthError;
import com.tpt.chat_task.modules.auth.dto.request.*;
import com.tpt.chat_task.modules.auth.dto.response.LoginResponse;
import com.tpt.chat_task.modules.auth.dto.response.VerifyOtpResponse;
import com.tpt.chat_task.modules.auth.entity.CustomUserDetails;
import com.tpt.chat_task.modules.auth.entity.Otp;
import com.tpt.chat_task.modules.auth.entity.Token;
import com.tpt.chat_task.modules.auth.jwt.JwtProvider;
import com.tpt.chat_task.modules.auth.repository.AuthProviderRepository;
import com.tpt.chat_task.modules.auth.repository.OtpRepository;
import com.tpt.chat_task.modules.auth.repository.TokenRepository;
import com.tpt.chat_task.modules.auth.service.OtpService;
import com.tpt.chat_task.modules.user.entity.User;
import com.tpt.chat_task.modules.user.enums.USER_STATUS;
import com.tpt.chat_task.modules.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock private UserRepository userRepository;
    @Mock private TokenRepository tokenRepository;
    @Mock private CustomUserDetailsService customUserDetailsService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtProvider jwtProvider;
    @Mock private OtpService otpService;
    @Mock private EmailService emailService;
    @Mock private OtpRepository otpRepository;
    @Mock private CacheBlackList cacheBlackList;
    @Mock private AuthProviderRepository authProviderRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void testLogin_Success() throws BadRequestException {
        // Arrange
        String email = "test@example.com";
        String password = "secret";
        String userAgent = "Mozilla/5.0";
        String ipAddress = "127.0.0.1";

        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);

        User user = User.builder()
                .email(email)
                .status(USER_STATUS.ACTIVE)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // Giả lập phương thức authenticate
        AuthServiceImpl spyService = Mockito.spy(authService);
        doReturn(authentication).when(spyService).authenticate(request);

        when(jwtProvider.generateAccessToken(authentication)).thenReturn("access-token");
        when(jwtProvider.generateRefreshToken(authentication)).thenReturn("refresh-token");

        // Act
        LoginResponse response = spyService.login(request, userAgent, ipAddress);

        // Assert
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());

        verify(tokenRepository).save(any(Token.class));
    }

    @Test
    void testLogin_Fail_UserPending() {
        // Arrange
        String email = "test@example.com";
        String password = "secret";
        String userAgent = "Mozilla/5.0";
        String ipAddress = "127.0.0.1";

        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);

        User user = User.builder()
                .email(email)
                .status(USER_STATUS.PENDING)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // Spy để mock lại authenticate
        AuthServiceImpl spyService = Mockito.spy(authService);
        doReturn(authentication).when(spyService).authenticate(request);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                spyService.login(request, userAgent, ipAddress)
        );

        assertEquals(AuthError.USER_PENDING, ex.getMessage());
    }

    @Test
    void testLogin_Fail_WrongEmailOrPassword() {
        // Arrange
        String email = "wrong@example.com";
        String password = "wrongpassword";
        String userAgent = "Mozilla/5.0";
        String ipAddress = "127.0.0.1";

        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);

        AuthServiceImpl spyService = Mockito.spy(authService);

        // Giả lập authenticate ném ra lỗi
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(spyService).authenticate(request);

        // Act & Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () ->
                spyService.login(request, userAgent, ipAddress)
        );

        assertEquals("Bad credentials", exception.getMessage());
    }

    @Test
    void testRegisterSuccess() throws Exception {
        // Arrange: chuẩn bị dữ liệu giả
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("123456");
        request.setFirstName("John");
        request.setLastName("Doe");

        // Giả lập userRepository.findByEmail trả về empty (chưa có user)
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // Giả lập passwordEncoder.encode trả về mật khẩu đã mã hóa
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");

        // Giả lập otpService.generateOtp trả về một mã otp giả
        when(otpService.generateOtp("test@example.com")).thenReturn("123456");

        // emailService.sendEmailWithHtml thì không làm gì (void method)

        // Act: gọi hàm register
        String result = authService.register(request);

        // Assert: verify kết quả và tương tác
        assertEquals("OTP was successfully sent to you email. Please verify.", result);

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).encode("123456");
        verify(userRepository).save(argThat(user ->
                user.getEmail().equals("test@example.com") &&
                        user.getPassword().equals("encoded-password") &&
                        user.getFirstName().equals("John") &&
                        user.getLastName().equals("Doe")
        ));
        verify(otpService).generateOtp("test@example.com");
        verify(emailService).sendEmailWithHtml(eq("test@example.com"), eq("VERIFY EMAIL TPT_SHOP"), anyString());
    }

    @Test
    void testRegisterFail_EmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("123456");
        request.setFirstName("John");
        request.setLastName("Doe");

        // Giả lập userRepository.findByEmail trả về empty (chưa có user)
        when(userRepository.findByEmail("test@example.com")).
                thenReturn(Optional.ofNullable(
                        User.builder()
                                .email("test@example.com")
                                .firstName("John")
                                .lastName("Doe")
                                .build())
                );

        // Act & Assert: kiểm tra exception được ném ra
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            authService.register(request);
        });

        // Có thể assert thêm message exception nếu muốn
        assertEquals(AuthError.EMAIL_ALREADY_EXISTS, exception.getMessage());

        // Verify userRepository.save không được gọi
        verify(userRepository, never()).save(any());
    }

    @Test
    void testForgotPassword_Success() throws MessagingException, IOException {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.ofNullable(
                        User.builder()
                                .email("test@example.com")
                                .firstName("John")
                                .lastName("Doe")
                                .build())
                );

        when(otpService.generateOtp("test@example.com")).thenReturn("123456");
        // When
        String result = authService.forgotPassword(request);

        // Then
        assertEquals("Otp was successfully sent to your email. Please verify your email.", result);
    }

    @Test
    void testForgotPassword_Fail() throws MessagingException, IOException {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

        // When
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            authService.forgotPassword(request);
        });

        // Then
        assertEquals(AuthError.USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void testVerifyOtpBeforeReset_Success() throws IOException {
        String otp = "123456";
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setOtp(otp);

        // Giả lập validateOtp không throw exception
        doNothing().when(otpService).validateOtp(otp);

        // Giả lập otpRepository.findByOtp trả về một Otp có email
        Otp mockedOtp = Otp.builder()
                .otp(otp)
                .email("test@example.com")
                .build();
        when(otpRepository.findByOtp(otp)).thenReturn(mockedOtp);

        // Act
        VerifyOtpResponse response = authService.verifyOtpBeforeReset(request);

        // Assert
        assertEquals("test@example.com", response.getEmail());

        // Verify
        verify(otpService).validateOtp(otp);
        verify(otpRepository).findByOtp(otp);
    }

    @Test
    void testResetPassword_Success() throws MessagingException, IOException {
        // Given (Mock)
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@example.com");
        request.setPassword("123456");
        request.setConfirmPassword("123456");

        // When (Act)
        User mockUser = User.builder()
                .email("test@example.com")
                .password("old-password")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode("123456")).thenReturn("encoded-new-password");

        User mockUpdatedUser = User.builder()
                .email("test@example.com")
                .password("encoded-new-password")
                .build();

        when(userRepository.save(mockUser)).thenReturn(mockUpdatedUser);

        // when
        String result = authService.resetPassword(request);

        // Then
        assertEquals("SUCCESS", result);
        assertEquals("encoded-new-password", mockUpdatedUser.getPassword());

        // Verify
        verify(userRepository).save(mockUser);
    }
}