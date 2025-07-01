package com.tpt.chat_task.modules.notification.repository;

import com.tpt.chat_task.modules.notification.entity.Notification;
import com.tpt.chat_task.modules.notification.entity.NotificationUser;
import com.tpt.chat_task.modules.notification.enums.NOTIFICATION_TYPE;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationUserRepository extends JpaRepository<NotificationUser, String> {

    @Query("SELECT nu.notification FROM NotificationUser nu WHERE nu.user.id = :userId")
    Page<Notification> findWorkspacesByUserId(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT nu.notification FROM NotificationUser nu JOIN Notification no ON no.id = nu.notification.id WHERE nu.user.id = :userId AND no.type =: type")
    Page<Notification> findWorkspacesByUserIdAndType(@Param("userId") String userId, @Param("type")NOTIFICATION_TYPE type, Pageable pageable);
}
