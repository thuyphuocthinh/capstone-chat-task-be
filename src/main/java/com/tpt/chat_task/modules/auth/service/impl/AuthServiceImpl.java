package com.tpt.chat_task.modules.auth.service.impl;

import com.tpt.chat_task.common.enums.RESPONSE_STATUS;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.infrastructure.email.service.EmailService;
import com.tpt.chat_task.infrastructure.email.utils.Template;
import com.tpt.chat_task.infrastructure.rabbitmq.utils.RabbitMQSchema;
import com.tpt.chat_task.infrastructure.redis.repository.CacheBlackList;
import com.tpt.chat_task.modules.auth.constant.AuthError;
import com.tpt.chat_task.modules.auth.dto.request.*;
import com.tpt.chat_task.modules.auth.dto.response.LoginResponse;
import com.tpt.chat_task.modules.auth.dto.response.VerifyOtpResponse;
import com.tpt.chat_task.modules.auth.entity.AuthProvider;
import com.tpt.chat_task.modules.auth.entity.CustomUserDetails;
import com.tpt.chat_task.modules.auth.entity.Otp;
import com.tpt.chat_task.modules.auth.entity.Token;
import com.tpt.chat_task.modules.auth.jwt.JwtProvider;
import com.tpt.chat_task.modules.auth.repository.AuthProviderRepository;
import com.tpt.chat_task.modules.auth.repository.OtpRepository;
import com.tpt.chat_task.modules.auth.repository.TokenRepository;
import com.tpt.chat_task.modules.auth.service.AuthService;
import com.tpt.chat_task.modules.auth.service.OtpService;
import com.tpt.chat_task.modules.user.entity.User;
import com.tpt.chat_task.modules.user.enums.USER_STATUS;
import com.tpt.chat_task.modules.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;

    private final TokenRepository tokenRepository;

    private final CustomUserDetailsService customUserDetailsService;

    private final PasswordEncoder passwordEncoder;

    private final JwtProvider jwtProvider;

    private final OtpService otpService;

    private final EmailService emailService;

    private final OtpRepository otpRepository;

    private final CacheBlackList cacheBlackList;

    private final AuthProviderRepository authProviderRepository;

    private final RabbitTemplate rabbitTemplate;

    public AuthServiceImpl(UserRepository userRepository, TokenRepository tokenRepository, CustomUserDetailsService customUserDetailsService, PasswordEncoder passwordEncoder, JwtProvider jwtProvider, OtpService otpService, EmailService emailService, OtpRepository otpRepository, CacheBlackList cacheBlackList, AuthProviderRepository authProviderRepository, RabbitTemplate rabbitTemplate) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.customUserDetailsService = customUserDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.otpService = otpService;
        this.emailService = emailService;
        this.otpRepository = otpRepository;
        this.cacheBlackList = cacheBlackList;
        this.authProviderRepository = authProviderRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Value("${jwt.refreshTokenExpiration}")
    private int jwtRefreshTokenExpirationMs;

    @Override
    public LoginResponse login(LoginRequest loginRequest, String userAgent, String ipAddress) throws BadRequestException {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        Authentication authentication = this.authenticate(loginRequest);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        if(user.getStatus() == USER_STATUS.PENDING) {
            throw new BadRequestException(AuthError.USER_PENDING);
        }

        if(user.getStatus() == USER_STATUS.INACTIVE) {
            throw new BadRequestException(AuthError.USER_NOT_ACTIVE);
        }

        String accessToken = this.jwtProvider.generateAccessToken(authentication);
        String refreshToken = this.jwtProvider.generateRefreshToken(authentication);

        Token token = Token.builder()
                .user(user)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .expiredAt(LocalDateTime.now().plus(jwtRefreshTokenExpirationMs, ChronoUnit.MILLIS))
                .refreshToken(refreshToken)
                .build();

        this.tokenRepository.save(token);

        log.info("Login::AccessToken: {}", accessToken);
        log.info("Login::RefreshToken: {}", refreshToken);

        try {
            String loginExchange = RabbitMQSchema.LOGIN_EXCHANGE;
            String loginRoutingKey = RabbitMQSchema.LOGIN_ROUTING_KEY;
            this.rabbitTemplate.convertAndSend(loginExchange, loginRoutingKey, user.getId());
            log.info("Sent to login queue");
        } catch (Exception e) {
            log.error("Error sending to login queue: {}", e.getMessage());
        }

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    Authentication authenticate(LoginRequest loginRequest) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(loginRequest.getEmail());
        if (userDetails == null) {
            throw new BadCredentialsException(AuthError.BAD_CREDENTIALS);
        }

        if(!passwordEncoder.matches(loginRequest.getPassword(), userDetails.getPassword())) {
            throw new BadCredentialsException(AuthError.BAD_CREDENTIALS);
        }

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Override
    public String register(RegisterRequest registerRequest) throws NotFoundException, IOException, MessagingException {
        String email = registerRequest.getEmail();
        String password = registerRequest.getPassword();
        String firstName = registerRequest.getFirstName();
        String lastName = registerRequest.getLastName();
        // 1. Check if email exists
        Optional<User> findUser = this.userRepository.findByEmail(email);
        if(findUser.isPresent()) {
            throw new BadRequestException(AuthError.EMAIL_ALREADY_EXISTS);
        }

        // 2. Save user
        User newUser = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .build();
        this.userRepository.save(newUser);

        // 3. Send otp
        String otp = this.otpService.generateOtp(email);
        String template = Template.getOtpHtmlTemplateAuth(otp);
        this.emailService.sendEmailWithHtml(email, "VERIFY EMAIL TPT_SHOP", template);
        return "OTP was successfully sent to you email. Please verify.";
    }

    private String verifyEmailService(String otp) throws BadRequestException {
        this.otpService.validateOtp(otp);
        Otp findOtp = this.otpRepository.findByOtp(otp);

        if(findOtp == null) {
            throw new NotFoundException(AuthError.OTP_NOT_FOUND);
        }

        return findOtp.getEmail();
    }

    @Override
    public LoginResponse verifyOtp(String otp, String ipAddress, String userAgent) throws NotFoundException, BadRequestException {
        String email = this.verifyEmailService(otp);
        // 1. Find user by email
        User findUser = this.userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException(AuthError.USER_NOT_FOUND));
        // 2. Update status
        findUser.setStatus(USER_STATUS.ACTIVE);
        this.userRepository.save(findUser);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // 3. Generate token
        String accessToken = jwtProvider.generateAccessToken(authentication);
        String refreshToken = jwtProvider.generateRefreshToken(authentication);

        Token token = Token.builder()
                .user(findUser)
                .refreshToken(refreshToken)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .expiredAt(LocalDateTime.now().plus(jwtRefreshTokenExpirationMs, ChronoUnit.MILLIS))
                .build();
        this.tokenRepository.save(token);

        // 4. Return
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public LoginResponse refreshToken(RefreshTokenRequest refreshTokenRequest, String ipAddress, String userAgent) throws NotFoundException {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        Token verify = this.jwtProvider.verifyRefreshToken(refreshToken);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(verify.getUser().getEmail());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        String newAccessToken = this.jwtProvider.generateAccessToken(authentication);
        String newRefreshToken = this.jwtProvider.generateRefreshToken(authentication);

        Token token = Token.builder()
                .user(verify.getUser())
                .refreshToken(newRefreshToken)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .expiredAt(LocalDateTime.now().plus(jwtRefreshTokenExpirationMs, ChronoUnit.MILLIS))
                .build();

        this.tokenRepository.save(token);
        verify.setRevoked(true);
        this.tokenRepository.save(verify);

        log.info("Refresh::AccessToken: {}", newAccessToken);
        log.info("Refresh::RefreshToken: {}", newRefreshToken);
        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Override
    public String logout(LogoutRequest logoutRequest) throws NotFoundException {
        String refreshToken = logoutRequest.getRefreshToken();
        String accessToken = logoutRequest.getAccessToken();
        // add access token to cache black list
        cacheBlackList.addNewAccessToken(accessToken);
        this.jwtProvider.verifyRefreshToken(refreshToken);
        return this.jwtProvider.revokeRefreshToken(refreshToken);
    }

    @Override
    public String forgotPassword(ForgotPasswordRequest forgotPasswordRequest) throws NotFoundException, IOException, MessagingException {
        String email = forgotPasswordRequest.getEmail();
        this.userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException(AuthError.USER_NOT_FOUND));
        String otp = this.otpService.generateOtp(email);
        String template = Template.getOtpHtmlTemplateAuth(otp);
        this.emailService.sendEmailWithHtml(email, "VERIFY EMAIL TPT_SHOP", template);
        return "Otp was successfully sent to your email. Please verify your email.";
    }

    @Override
    public VerifyOtpResponse verifyOtpBeforeReset(VerifyOtpRequest verifyOtpRequest) throws NotFoundException, IOException {
        String otp = verifyOtpRequest.getOtp();
        this.otpService.validateOtp(otp);
        Otp findOtp = this.otpRepository.findByOtp(otp);
        return VerifyOtpResponse.builder()
                .email(findOtp.getEmail())
                .build();
    }

    @Override
    public String resetPassword(ResetPasswordRequest resetPasswordRequest) throws NotFoundException, IOException, MessagingException {
        String password = resetPasswordRequest.getPassword();
        String confirmPassword = resetPasswordRequest.getConfirmPassword();
        String email = resetPasswordRequest.getEmail();

        if(!password.equals(confirmPassword)) {
            throw new BadRequestException(AuthError.PASSWORD_MISMATCH);
        }

        User findUser = this.userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException(AuthError.USER_NOT_FOUND));
        findUser.setPassword(passwordEncoder.encode(password));
        this.userRepository.save(findUser);

        return RESPONSE_STATUS.SUCCESS.toString();
    }

    private void saveAuthProvider(GoogleUserDTO googleUserDTO, User user) {
        AuthProvider authProvider = AuthProvider.builder()
                .providerId(googleUserDTO.getSub())
                .user(user)
                .build();

        this.authProviderRepository.save(authProvider);
    }

    @Override
    public LoginResponse googleLogin(GoogleUserDTO googleUserDTO, String userAgent, String ipAddress) throws NotFoundException, IOException, MessagingException {
        String email = googleUserDTO.getEmail();

        User createdOrExist = this.userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                    .email(email)
                    .firstName(googleUserDTO.getGivenName())
                    .lastName(googleUserDTO.getFamilyName())
                    .status(USER_STATUS.ACTIVE)
                    .build();
            User savedUser = this.userRepository.save(newUser);
            this.saveAuthProvider(googleUserDTO, savedUser);
            return savedUser;
        });

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Tạo AccessToken & RefreshToken từ `authentication`
        String accessToken = jwtProvider.generateAccessToken(authentication);
        String refreshToken = jwtProvider.generateRefreshToken(authentication);

        Token token = Token.builder()
                .user(createdOrExist)
                .refreshToken(refreshToken)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .expiredAt(LocalDateTime.now().plus(jwtRefreshTokenExpirationMs, ChronoUnit.MILLIS))
                .build();
        this.tokenRepository.save(token);

        return LoginResponse.builder().
                accessToken(accessToken).
                refreshToken(refreshToken).
                build();
    }
}
