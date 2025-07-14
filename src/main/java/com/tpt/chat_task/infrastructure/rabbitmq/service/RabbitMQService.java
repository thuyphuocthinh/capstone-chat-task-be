package com.tpt.chat_task.infrastructure.rabbitmq.service;

import org.apache.coyote.BadRequestException;

public interface RabbitMQService {
    void addNewQueue(String listenerId, String queueName, String exchangeName, String routingKey) throws BadRequestException;
    void addQueueToListener(String listenerId, String queueName);
    void removeQueueFromListener(String listenerId, String queueName) throws BadRequestException;
    boolean checkQueueExistOnListener(String listenerId, String queueName);
    void restoreQueuesByListener(String listenerId);
}