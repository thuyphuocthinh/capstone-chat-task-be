package com.tpt.chat_task.modules.notification.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class NotificationUserId {
    private String notificationId;
    private String userId;
}
