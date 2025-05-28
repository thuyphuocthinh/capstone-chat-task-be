package com.tpt.chat_task.modules.notification.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class NotificationUserId {
    private String notificationId;
    private String userId;
}
