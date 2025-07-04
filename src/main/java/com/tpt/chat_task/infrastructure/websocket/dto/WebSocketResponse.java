package com.tpt.chat_task.infrastructure.websocket.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tpt.chat_task.infrastructure.rabbitmq.enums.PUSH_NOTIFICATION_TYPE;
import com.tpt.chat_task.modules.notification.enums.NOTIFICATION_TYPE;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebSocketResponse {
    private NOTIFICATION_TYPE notificationType;
    private PUSH_NOTIFICATION_TYPE type;
    private String action;
    private Object data;
}
