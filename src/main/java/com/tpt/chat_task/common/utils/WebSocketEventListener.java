package com.tpt.chat_task.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@Slf4j
public class WebSocketEventListener {
    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        if (event.getUser() != null) {
            log.info("WebSocket connected with user: {}", event.getUser().getName());
        } else {
            log.info("Connected user is null!");
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        if (event.getUser() != null) {
            log.info("WebSocket disconnected: {}", event.getUser().getName());
        } else {
            log.info("Disconnected user is null!");
        }
    }
}
