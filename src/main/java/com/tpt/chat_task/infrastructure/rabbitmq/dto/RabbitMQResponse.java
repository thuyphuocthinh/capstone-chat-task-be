package com.tpt.chat_task.infrastructure.rabbitmq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RabbitMQResponse {
    String message;
    String messageId;
    String routingKey;
    String exchangeName;
    String exchangeType;
}
