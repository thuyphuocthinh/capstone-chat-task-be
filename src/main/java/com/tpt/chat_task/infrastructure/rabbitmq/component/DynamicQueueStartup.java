package com.tpt.chat_task.infrastructure.rabbitmq.component;

import com.tpt.chat_task.infrastructure.rabbitmq.service.RabbitMQService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DynamicQueueStartup {
    private final RabbitMQService rabbitmqService;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        String listenerId = "chat-listener";
        rabbitmqService.restoreQueuesByListener(listenerId);
    }
}
