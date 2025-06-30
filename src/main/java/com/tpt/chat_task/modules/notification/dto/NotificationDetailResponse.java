package com.tpt.chat_task.modules.notification.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.tpt.chat_task.modules.notification.enums.NOTIFICATION_TYPE;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class NotificationDetailResponse {
    private String id;
    private String userId;
    private String title;
    private NOTIFICATION_TYPE type;
    private JsonNode data;
}
