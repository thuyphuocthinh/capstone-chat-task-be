package com.tpt.chat_task.modules.auth.jwt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JwtProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.accessTokenExpiration}")
    private int accessTokenExpirationMs;

    @Value("${jwt.refreshTokenExpiration}")
    private int refreshTokenExpirationMs;


}
