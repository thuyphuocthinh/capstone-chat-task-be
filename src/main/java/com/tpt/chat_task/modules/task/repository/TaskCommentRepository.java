package com.tpt.chat_task.modules.task.repository;

import com.tpt.chat_task.modules.task.entity.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, String> {

    @Query(value = """
        SELECT *
        FROM task_comments
        WHERE task_id = :taskId
        ORDER BY created_at DESC
    """, nativeQuery = true)
    List<TaskComment> findAllByTaskId(@Param("taskId") String taskId);
}
