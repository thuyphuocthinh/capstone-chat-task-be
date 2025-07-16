package com.tpt.chat_task.config.websocket;

import com.tpt.chat_task.modules.auth.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    private final JwtProvider jwtService;


    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        // Lấy Authorization header
        List<String> authHeaders = request.getHeaders().get("Authorization");
        if (authHeaders == null || authHeaders.isEmpty()) return null;

        String token = authHeaders.get(0).replace("Bearer ", "");
        String userID = jwtService.getIdFromToken(token);

        // Tạo Principal từ username (hoặc từ UserDetails)
        return new UsernamePasswordAuthenticationToken(userID, null, List.of());
    }
}
