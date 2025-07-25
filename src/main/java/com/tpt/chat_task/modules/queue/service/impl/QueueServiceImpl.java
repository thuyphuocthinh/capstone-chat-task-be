package com.tpt.chat_task.modules.queue.service.impl;

import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.queue.constant.QueueError;
import com.tpt.chat_task.modules.queue.dto.request.QueueRequest;
import com.tpt.chat_task.modules.queue.dto.response.QueueResponse;
import com.tpt.chat_task.modules.queue.entity.Queue;
import com.tpt.chat_task.modules.queue.repository.QueueRepository;
import com.tpt.chat_task.modules.queue.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class QueueServiceImpl implements QueueService {
    private final QueueRepository queueRepository;

    @Override
    public void addNewQueue(QueueRequest queueRequest) throws BadRequestException {
        if(queueRepository.existsByQueueNameAndExchangeNameAndRoutingKey(
                queueRequest.getQueueName(),
                queueRequest.getExchangeName(),
                queueRequest.getRoutingKey(),
                queueRequest.getListenerId())
        ) {
            log.error("Queue already exists: {}", QueueError.QUEUE_ALREADY_EXISTS);
            return;
        }
        Queue queue = Queue.builder()
                .queueName(queueRequest.getQueueName())
                .listenerId(queueRequest.getListenerId())
                .exchangeName(queueRequest.getExchangeName())
                .routingKey(queueRequest.getRoutingKey())
                .build();
        this.queueRepository.save(queue);
    }

    @Override
    public void removeQueue(String queueName) throws NotFoundException, BadRequestException {
        if(!queueRepository.existsByQueueName(queueName)) {
            log.error("Queue does not exist: {}", QueueError.QUEUE_DOES_NOT_EXIST);
            return;
        }
        queueRepository.deleteAllByQueueName(queueName);
    }

    // TODO: remove queue by exchange name  , queue name, listener id, routing key


    @Override
    public List<QueueResponse> getListQueuesByListenerId(String listenerId) throws NotFoundException {
        List<Queue> queues = this.queueRepository.findAllByListenerId(listenerId);
        return queues.stream().map(q -> {
            return QueueResponse.builder()
                    .id(q.getId())
                    .queueName(q.getQueueName())
                    .listenerId(q.getListenerId())
                    .exchangeName(q.getExchangeName())
                    .routingKey(q.getRoutingKey())
                    .build();
        }).toList();
    }

    @Override
    public void removeQueueByNameListenerRoutingExchange(String queueName, String listenerId, String routingExchangeName, String routingKey) throws NotFoundException, BadRequestException {
        if(!queueRepository.existsByQueueNameAndExchangeNameAndRoutingKey(queueName, routingExchangeName, routingKey, listenerId)) {
            log.error("Queue does not exist: {}", QueueError.QUEUE_DOES_NOT_EXIST);
            return;
        }

        queueRepository.deleteByQueueNameListenerRoutingKeyExchange(queueName, listenerId, routingExchangeName, routingKey);
    }
}
