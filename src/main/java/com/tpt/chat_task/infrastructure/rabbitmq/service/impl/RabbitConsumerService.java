package com.tpt.chat_task.infrastructure.rabbitmq.service.impl;

import com.tpt.chat_task.infrastructure.rabbitmq.dto.RabbitMQResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class RabbitConsumerService {
    @RabbitListener(id = "chat-task-listener", queues = {}, concurrency = "4")
    public void receiver(RabbitMQResponse response, Message message) {
        log.info("Received Message from rabbit : {}", response.toString());
        String queueName = message.getMessageProperties().getConsumerQueue();
        log.info("Received from queue: {}", queueName);
        try {
            log.info("completed {} task", response.toString());
        } catch (Exception e) {
            log.error("Error on running test set");
            log.error("Error message : {}", e.getMessage());
            log.error("Error trace : {}", (Object) e.getStackTrace());
        }
    }
}
