package com.tpt.chat_task.infrastructure.rabbitmq.component;

import com.tpt.chat_task.infrastructure.rabbitmq.service.RabbitMQService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DynamicQueueStartup {
    private final RabbitMQService rabbitmqService;
    @PostConstruct
    public void onStartup() {
        String listenerId = "chat-listener";
        rabbitmqService.restoreQueuesByListener(listenerId);
    }
}
