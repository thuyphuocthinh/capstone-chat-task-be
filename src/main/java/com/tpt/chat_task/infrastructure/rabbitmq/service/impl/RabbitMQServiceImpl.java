package com.tpt.chat_task.infrastructure.rabbitmq.service.impl;

import com.tpt.chat_task.infrastructure.rabbitmq.service.RabbitMQService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Log4j2
public class RabbitMQServiceImpl implements RabbitMQService {

    private final AmqpAdmin rabbitAdmin;

    private final RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;

    @Override
    public void addNewQueue(String listenerId, String queueName, String exchangeName, String routingKey) {
        DirectExchange exchange = new DirectExchange(exchangeName);
        rabbitAdmin.declareExchange(exchange);
        Queue queue = new Queue(queueName, true, false, false);
        Binding binding = new Binding(
                queueName,
                Binding.DestinationType.QUEUE,
                exchangeName,
                routingKey,
                null
        );
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareBinding(binding);
        this.addQueueToListener(listenerId, queueName);
    }

    @Override
    public void addQueueToListener(String listenerId, String queueName) {
        log.info("Adding queue {} to listener {}", queueName, listenerId);
        if(!checkQueueExistOnListener(listenerId, queueName)) {
            this.getRabbitListenerContainer(listenerId).addQueueNames(queueName);
            log.info("Queue {} added to listener {}", queueName, listenerId);
        } else {
            log.info("Queue {} already exists on listener {}", queueName, listenerId);
        }
    }

    private AbstractMessageListenerContainer getRabbitListenerContainer(String listenerId) {
        log.info("Getting RabbitListenerContainer for listenerId {}", listenerId);
        return (AbstractMessageListenerContainer) this.rabbitListenerEndpointRegistry.getListenerContainer(listenerId);
    }

    @Override
    public void removeQueueFromListener(String listenerId, String queueName) {
        log.info("Removing queue {} from listener {}", queueName, listenerId);
        if(checkQueueExistOnListener(listenerId, queueName)) {
            this.getRabbitListenerContainer(listenerId).removeQueueNames(queueName);
            log.info("Queue {} removed from listener {}", queueName, listenerId);
            this.rabbitAdmin.deleteQueue(queueName);
        } else {
            log.info("Queue {} does not exist on listener {}", queueName, listenerId);
        }
    }

    @Override
    public boolean checkQueueExistOnListener(String listenerId, String queueName) {
        try {
            log.info("Checking if queue {} exists on listener {}", queueName, listenerId);
            String[] queueNames = this.getRabbitListenerContainer(listenerId).getQueueNames();
            if(queueNames.length > 0) {
                for(String name : queueNames) {
                    if(name.equals(queueName)) {
                        log.info("Checking result - Queue {} exists on listener {}", queueName, listenerId);
                        return true;
                    }
                }
            } else {
                log.info("there is no queue exist on listener");
                return false;
            }
        } catch (Exception e) {
            log.error("Error on checking queue exist on listener");
            log.error("error message : {}", e.getMessage());
            log.error("trace : {}", Arrays.toString(e.getStackTrace()));
            return false;
        }
        return false;
    }
}