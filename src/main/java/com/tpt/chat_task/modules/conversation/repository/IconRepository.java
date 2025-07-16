package com.tpt.chat_task.modules.conversation.repository;

import com.tpt.chat_task.modules.conversation.entity.Icon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IconRepository extends JpaRepository<Icon, String> {
}
