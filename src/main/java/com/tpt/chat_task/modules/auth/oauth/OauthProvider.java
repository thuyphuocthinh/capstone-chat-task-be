package com.tpt.chat_task.modules.auth.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.json.webtoken.JsonWebSignature;
import com.google.auth.oauth2.TokenVerifier;
import com.tpt.chat_task.modules.auth.dto.request.GoogleUserDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class OauthProvider {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String CLIENT_SECRET;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String CLIENT_ID;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String REDIRECT_URI;

    private final ObjectMapper objectMapper;

    private final TokenVerifier verifier;

    public OauthProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.verifier = TokenVerifier.newBuilder().setAudience(CLIENT_ID).build();
    }

    public GoogleUserDTO verifyToken(String idTokenString) throws BadRequestException {
        log.info("Google idTokenString:::: {}", idTokenString);
        // Xác thực token
        JsonWebSignature idToken;
        try {
            idToken = this.verifier.verify(idTokenString);
        } catch (Exception e) {
            throw new BadRequestException("Invalid ID Token");
        }

        // Lấy payload từ ID Token
        JsonWebSignature.Payload payload = idToken.getPayload();

        String email = (String) payload.get("email");
        String givenName = (String) payload.get("given_name");
        String familyName = (String) payload.get("family_name");
        String sub = payload.getSubject();

        return GoogleUserDTO.builder()
                .sub(sub)
                .email(email)
                .familyName(familyName)
                .givenName(givenName)
                .build();
    }

    public GoogleUserDTO getUserInfoFromGoogle(String code) {
        // 1️⃣ Đổi "code" lấy "access_token"
        String tokenEndpoint = "https://oauth2.googleapis.com/token";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", CLIENT_ID);
        params.add("client_secret", CLIENT_SECRET);
        params.add("redirect_uri", REDIRECT_URI);
        params.add("grant_type", "authorization_code");
        params.add("code", code);

        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenEndpoint, new HttpEntity<>(params), Map.class);
        String accessToken = (String) tokenResponse.getBody().get("access_token");

        // 2️⃣ Lấy thông tin user từ Google
        String userInfoEndpoint = "https://www.googleapis.com/oauth2/v3/userinfo";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        ResponseEntity<Map> userResponse = restTemplate.exchange(userInfoEndpoint, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        Map<String, Object> userInfo = userResponse.getBody();

        return objectMapper.convertValue(userInfo, GoogleUserDTO.class);
    }
}