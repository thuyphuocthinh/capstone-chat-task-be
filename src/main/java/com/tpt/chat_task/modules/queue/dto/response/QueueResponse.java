package com.tpt.chat_task.modules.queue.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueueResponse {
    private String id;
    private String queueName;
    private String listenerId;
    private String exchangeName;
    private String routingKey;
}
