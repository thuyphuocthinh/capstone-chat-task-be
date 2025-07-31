package com.tpt.chat_task.modules.auth.jwt;

import com.tpt.chat_task.common.enums.RESPONSE_STATUS;
import com.tpt.chat_task.modules.auth.constant.AuthError;
import com.tpt.chat_task.modules.auth.entity.CustomUserDetails;
import com.tpt.chat_task.modules.auth.entity.Token;
import com.tpt.chat_task.modules.auth.repository.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JwtProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.accessTokenExpiration}")
    private int accessTokenExpirationMs;

    @Value("${jwt.refreshTokenExpiration}")
    private int refreshTokenExpirationMs;

    private SecretKey secretKey;

    private final TokenRepository tokenRepository;

    public JwtProvider(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // generate access token
    public String generateAccessToken(Authentication authentication) {
        log.info("[JwtTokenGenerator:generateAccessToken] Token Creation Started for:{}", authentication.getName());
        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .claim("id", userPrincipal.getUserId())
                .claim("email", userPrincipal.getUsername())
                .claim("authorities", roles)
                .signWith(secretKey)
                .compact();
    }

    // generate refresh token
    public String generateRefreshToken(Authentication authentication) {
        log.info("[JwtTokenGenerator:generateRefreshToken] Token Creation Started for: {}", authentication.getName());
        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();

        return Jwts.builder()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
                .claim("id", userPrincipal.getUserId())
                .signWith(secretKey)
                .compact();
    }

    // get email from access token
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
        return String.valueOf(claims.get("email"));
    }

    // get id from access token
    public String getIdFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
        return String.valueOf(claims.get("id"));
    }

    // verify refresh token
    public Token verifyRefreshToken(String refreshToken) {
        Token refreshTokenObj = tokenRepository.findByRefreshToken(refreshToken).orElseThrow(() -> new BadCredentialsException(AuthError.REFRESH_TOKEN_INVALID));
        if(refreshTokenObj.getExpiredAt().isBefore(
                LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())
        )) {
            throw new BadCredentialsException(AuthError.REFRESH_TOKEN_EXPIRED);
        }
        if(refreshTokenObj.isRevoked()) {
            throw new BadCredentialsException(AuthError.REFRESH_TOKEN_INVALID);
        }
        return refreshTokenObj;
    }

    // revoke refresh token
    public String revokeRefreshToken(String refreshToken) {
        Token refreshTokenObj = tokenRepository.findByRefreshToken(refreshToken).orElseThrow(() -> new BadCredentialsException(AuthError.REFRESH_TOKEN_INVALID));
        if(refreshTokenObj.getExpiredAt().isBefore(
                LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())
        )) {
            throw new BadCredentialsException(AuthError.REFRESH_TOKEN_EXPIRED);
        } else {
            refreshTokenObj.setRevoked(true);
            tokenRepository.save(refreshTokenObj);
        }
        return RESPONSE_STATUS.SUCCESS.toString();
    }
}
