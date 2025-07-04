package com.tpt.chat_task.infrastructure.rabbitmq.service;

public interface RabbitMQService {
    void addNewQueue(String listenerId, String queueName, String exchangeName, String routingKey);
    void addQueueToListener(String listenerId, String queueName);
    void removeQueueFromListener(String listenerId, String queueName);
    boolean checkQueueExistOnListener(String listenerId, String queueName);
}