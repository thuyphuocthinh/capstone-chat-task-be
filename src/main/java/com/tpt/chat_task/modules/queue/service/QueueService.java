package com.tpt.chat_task.modules.queue.service;

import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.queue.dto.request.QueueRequest;
import com.tpt.chat_task.modules.queue.dto.response.QueueResponse;
import org.apache.coyote.BadRequestException;

import java.util.List;

public interface QueueService {
    public QueueResponse addNewQueue(QueueRequest queueRequest) throws BadRequestException;
    public void removeQueue(String id) throws NotFoundException;
    public List<QueueResponse> getListQueuesByListenerId(String listenerId) throws NotFoundException;
}
