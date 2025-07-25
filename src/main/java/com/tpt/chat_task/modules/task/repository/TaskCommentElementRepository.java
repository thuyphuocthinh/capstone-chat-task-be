package com.tpt.chat_task.modules.task.repository;

import com.tpt.chat_task.modules.task.entity.TaskCommentElement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskCommentElementRepository extends JpaRepository<TaskCommentElement, String> {
}
