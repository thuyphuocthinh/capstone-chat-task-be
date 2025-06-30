package com.tpt.chat_task.modules.notification.repository;

import com.tpt.chat_task.modules.notification.entity.NotificationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationUserRepository extends JpaRepository<NotificationUser, String> {
}
