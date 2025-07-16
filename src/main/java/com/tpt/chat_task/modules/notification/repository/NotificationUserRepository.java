package com.tpt.chat_task.modules.notification.repository;

import com.tpt.chat_task.modules.notification.entity.Notification;
import com.tpt.chat_task.modules.notification.entity.NotificationUser;
import com.tpt.chat_task.modules.notification.entity.NotificationUserId;
import com.tpt.chat_task.modules.notification.enums.NOTIFICATION_TYPE;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationUserRepository extends JpaRepository<NotificationUser, NotificationUserId> {

    @Query("SELECT nu.notification FROM NotificationUser nu WHERE nu.id.userId = :userId")
    Page<Notification> findNotificationsByUserId(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT n FROM NotificationUser nu JOIN nu.notification n WHERE nu.id.userId = :userId AND n.type = :type")
    Page<Notification> findNotificationsByUserIdAndType(@Param("userId") String userId, @Param("type") NOTIFICATION_TYPE type, Pageable pageable);

    @Modifying
    @Query("""
        UPDATE NotificationUser nu
        SET nu.isRead = true
        WHERE nu.user.id = :userId
    """)
    void markReadAllNotifications(@Param("userId") String userId);

    @Modifying
    @Query(value = """
        UPDATE NotificationUser nu
        SET nu.is_read = true
        WHERE nu.user_id = :userId AND nu.notification_id = :notificationId
    """, nativeQuery = true)
    void markReadNotification(@Param("userId") String userId, @Param("notificationId") String notificationId);

    @Query(value = """
        SELECT COUNT(*)
        FROM NotificationUser nu
        WHERE nu.user_id = :userId AND nu.is_read = false
    """, nativeQuery = true)
    int countUnreadAllNotifications(@Param("userId") String userId);

    @Query(value = """
        SELECT COUNT(*)
        FROM NotificationUser nu
        WHERE nu.user_id = :userId AND nu.is_read = false AND nu.type = :type
    """, nativeQuery = true)
    int countUnreadTypeNotifications(@Param("userId") String userId, @Param("type") NOTIFICATION_TYPE type);
}
