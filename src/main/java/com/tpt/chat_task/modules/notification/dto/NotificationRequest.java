package com.tpt.chat_task.modules.notification.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.tpt.chat_task.modules.notification.enums.NOTIFICATION_TYPE;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class NotificationRequest {
    @NotNull(message = "Notification type is required")
    private NOTIFICATION_TYPE type;

    @NotBlank(message = "Title cannot be blank")
    private String title;

    @NotNull(message = "Data cannot be null")
    private JsonNode data;

    @NotBlank(message = "User id cannot be blank")
    private String userId;
}
