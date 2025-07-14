package com.tpt.chat_task.modules.queue.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class QueueRequest {
    private String queueName;
    private String listenerId;
    private String exchangeName;
    private String routingKey;
}
