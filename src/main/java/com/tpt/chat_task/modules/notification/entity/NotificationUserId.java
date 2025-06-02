package com.tpt.chat_task.modules.notification.entity;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public class NotificationUserId {
    private UUID notificationId;
    private UUID userId;
}
