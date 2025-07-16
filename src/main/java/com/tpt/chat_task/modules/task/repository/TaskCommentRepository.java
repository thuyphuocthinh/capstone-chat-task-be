package com.tpt.chat_task.modules.task.repository;

import com.tpt.chat_task.modules.task.entity.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, String> {
}
