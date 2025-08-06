package com.tpt.chat_task.infrastructure.rabbitmq.service.impl;

import com.tpt.chat_task.infrastructure.rabbitmq.service.RabbitMQService;
import com.tpt.chat_task.infrastructure.rabbitmq.utils.RabbitMQSchema;
import com.tpt.chat_task.modules.queue.dto.request.QueueRequest;
import com.tpt.chat_task.modules.queue.dto.response.QueueResponse;
import com.tpt.chat_task.modules.queue.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.coyote.BadRequestException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class RabbitMQServiceImpl implements RabbitMQService {

    private final AmqpAdmin rabbitAdmin;

    private final RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;

    private final QueueService queueService;

    @Override
    public void addNewQueue(String listenerId, String queueName, String exchangeName, String routingKey) throws BadRequestException {
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
        QueueRequest queueEntity = QueueRequest.builder()
                .queueName(queueName)
                .exchangeName(exchangeName)
                .routingKey(routingKey)
                .listenerId(listenerId)
                .build();
        // save queue name in database
        this.queueService.addNewQueue(queueEntity);
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
    public void removeQueueFromListener(String listenerId, String queueName) throws BadRequestException {
        log.info("Removing queue {} from listener {}", queueName, listenerId);
        if(checkQueueExistOnListener(listenerId, queueName)) {
            this.getRabbitListenerContainer(listenerId).removeQueueNames(queueName);
            log.info("Queue {} removed from listener {}", queueName, listenerId);
            this.rabbitAdmin.deleteQueue(queueName);
            // delete queue name from database
            this.queueService.removeQueue(queueName);
        } else {
            log.info("Queue {} not exist on listener {}", queueName, listenerId);
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

    @Override
    public void restoreQueuesByListener(String listenerId) {
        List<QueueResponse> queueResponses = this.queueService.getListQueuesByListenerId(listenerId);
        log.info("queues responses: {}", queueResponses.toString());
        queueResponses.forEach(queue -> {
            String queueName = queue.getQueueName();
            if (!checkQueueExistOnListener(listenerId, queueName)) {
                this.getRabbitListenerContainer(listenerId).addQueueNames(queueName);
                log.info("Bound queue [{}] to listener [{}]", queueName, listenerId);
            } else {
                log.info("Queue [{}] already bound to listener [{}]", queueName, listenerId);
            }
        });
        log.info("All dynamic queues restored.");
    }

    @Override
    public void unbindQueue(String listenerId, String queueName, String routingKey, String exchangeName) throws BadRequestException {
        if(checkQueueExistOnListener(listenerId, queueName)) {
            this.getRabbitListenerContainer(listenerId).removeQueueNames(queueName);
            Binding binding = BindingBuilder
                    .bind(new Queue(queueName))
                    .to(new DirectExchange(exchangeName))
                    .with(routingKey);
            this.rabbitAdmin.removeBinding(binding);
            this.queueService.removeQueueByNameListenerRoutingExchange(queueName, listenerId, exchangeName, routingKey);
            log.info("Queue {} unbind from listener {}", queueName, listenerId);
        } else {
            log.info("Queue {} does not exist on listener {}", queueName, listenerId);
        }
    }
}