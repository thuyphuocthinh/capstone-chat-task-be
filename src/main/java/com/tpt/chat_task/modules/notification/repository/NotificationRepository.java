package com.tpt.chat_task.modules.notification.repository;

import com.tpt.chat_task.modules.notification.entity.Notification;
import com.tpt.chat_task.modules.notification.enums.NOTIFICATION_TYPE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    @Query(value = """
    SELECT * FROM notifications
    WHERE type = :type
      AND data ->> 'id' = :messageId
      AND data ->> 'senderId' = :userId
    LIMIT 1
    """, nativeQuery = true)
    Optional<Notification> findReactionNotification(@Param("type") NOTIFICATION_TYPE type, @Param("messageId") String messageId, @Param("userId") String userId);

    @Query(value = """
    SELECT * FROM notifications
    WHERE type = :type
      AND data ->> 'id' = :taskId
      AND data ->> 'senderId' = :userId
    LIMIT 1
    """, nativeQuery = true)
    Optional<Notification> findMentionTaskNotification(@Param("type") NOTIFICATION_TYPE type, @Param("taskId") String taskId, @Param("userId") String userId);
}
