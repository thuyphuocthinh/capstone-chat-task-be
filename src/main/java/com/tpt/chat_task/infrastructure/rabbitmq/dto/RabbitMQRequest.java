package com.tpt.chat_task.infrastructure.rabbitmq.dto;

import com.tpt.chat_task.infrastructure.rabbitmq.enums.EXCHANGE_TYPE;
import com.tpt.chat_task.infrastructure.rabbitmq.enums.PUSH_NOTIFICATION_TYPE;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RabbitMQRequest {
    private String routingKey;
    private EXCHANGE_TYPE exchangeType;
    private PUSH_NOTIFICATION_TYPE pushNotificationType;
    private String pushNotificationAction;
    private Object payload;
}
