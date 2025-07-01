package com.tpt.chat_task.modules.notification.service;

import com.tpt.chat_task.common.dto.SuccessResponseWithMetadata;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.notification.dto.NotificationRequest;
import com.tpt.chat_task.modules.notification.enums.NOTIFICATION_TYPE;

public interface NotificationService {
    public void saveNotification(NotificationRequest notificationRequest);
    public void deleteNotification(String id) throws NotFoundException;
    public SuccessResponseWithMetadata<?> getNotificationsByUser(String userId, Integer paging, Integer page) throws NotFoundException;
    public SuccessResponseWithMetadata<?> getNotificationsByUserAndType(String userId, NOTIFICATION_TYPE type, Integer paging, Integer page) throws NotFoundException;
}
