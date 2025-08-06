package com.tpt.chat_task.infrastructure.rabbitmq.component;

import com.tpt.chat_task.infrastructure.rabbitmq.service.RabbitMQService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Component
@RequiredArgsConstructor
public class DynamicQueueStartup {
    private final RabbitMQService rabbitmqService;

    private final ExecutorService executorService;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        List<String> listenerIds = List.of("chat-listener", "task-listener");
        listenerIds.forEach(id -> executorService.submit(() -> rabbitmqService.restoreQueuesByListener(id)));
    }
}
