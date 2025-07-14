package com.tpt.chat_task.modules.queue.service.impl;

import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.queue.constant.QueueError;
import com.tpt.chat_task.modules.queue.dto.request.QueueRequest;
import com.tpt.chat_task.modules.queue.dto.response.QueueResponse;
import com.tpt.chat_task.modules.queue.entity.Queue;
import com.tpt.chat_task.modules.queue.repository.QueueRepository;
import com.tpt.chat_task.modules.queue.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QueueServiceImpl implements QueueService {
    private final QueueRepository queueRepository;

    @Override
    public QueueResponse addNewQueue(QueueRequest queueRequest) throws BadRequestException {
        if(queueRepository.existsByQueueName(queueRequest.getQueueName())) {
            throw new BadRequestException(QueueError.QUEUE_NAME_ALREADY_EXISTS);
        }
        Queue queue = Queue.builder()
                .queueName(queueRequest.getQueueName())
                .listenerId(queueRequest.getListenerId())
                .exchangeName(queueRequest.getExchangeName())
                .routingKey(queueRequest.getRoutingKey())
                .build();
        queue = queueRepository.save(queue);
        return QueueResponse.builder()
                .id(queue.getId())
                .queueName(queue.getQueueName())
                .listenerId(queue.getListenerId())
                .exchangeName(queue.getExchangeName())
                .routingKey(queue.getRoutingKey())
                .build();
    }

    @Override
    public void removeQueue(String id) throws NotFoundException {
        this.queueRepository.findById(id).orElseThrow(() -> new NotFoundException(QueueError.QUEUE_DOES_NOT_EXIST));
        queueRepository.deleteById(id);
    }

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
}
